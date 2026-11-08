// (C) 2012 uchicom
package com.uchicom.plate;

import com.uchicom.plate.Starter.StarterKind;
import com.uchicom.plate.dto.MethodDto;
import com.uchicom.plate.dto.OrderDto;
import com.uchicom.plate.dto.PlateConfig;
import com.uchicom.plate.dto.ReleaseDto;
import com.uchicom.plate.dto.ScheduleDto;
import com.uchicom.plate.dto.StarterDto;
import com.uchicom.plate.enumeration.CpState;
import com.uchicom.plate.enumeration.KeyState;
import com.uchicom.plate.enumeration.RecoveryMethod;
import com.uchicom.plate.exception.CmdException;
import com.uchicom.plate.factory.di.DIFactory;
import com.uchicom.plate.scheduler.Schedule;
import com.uchicom.plate.scheduler.ScheduleFactory;
import com.uchicom.plate.service.DeployService;
import com.uchicom.plate.service.GithubService;
import com.uchicom.plate.util.Crypt;
import com.uchicom.util.Parameter;
import com.uchicom.util.ThrowingConsumer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.yaml.snakeyaml.Yaml;

/**
 * serverとbatchを管理するミドルウェア.
 *
 * @author Uchiyama Shigeki
 */
public class Main {

  public Timer timer = new Timer();

  /** リリースディレクトリ監視スレッド */
  private Thread watchThread;

  /**
   * コマンド引数を使用してサーバーを起動する。 ポート番号を指定してコマンドを受け付けるポート番号を指定する。
   * 任意でデフォルトの設定ファイル名を指定する。指定されている場合は設定ファイルをロードする。
   */
  public static void main(String[] args) {
    var parameter = new Parameter(args);
    var main = DIFactory.main();

    main.address = parameter.get("host", Constants.DEFAULT_ADDRESS);
    main.port = parameter.getInt("port", Constants.DEFAULT_PORT);
    if (parameter.is("file")) {
      // 設定ファイルロード処理
      main.load(parameter.getFile("file"));
    }

    Runtime.getRuntime().addShutdownHook(new Thread(main::shutdown));
    main.execute();
  }

  /** スレッドプール */
  ExecutorService exec = Executors.newFixedThreadPool(Constants.DEFAULT_POOL_SIZE);

  /** コンソール待ちうけアドレス */
  private String address = Constants.DEFAULT_ADDRESS;

  /** ポート */
  private int port = Constants.DEFAULT_PORT;

  /** キー情報保持マップ */
  private Map<String, Porter> portMap = new HashMap<String, Porter>();

  /** ユーザー名 */
  private String user;

  private File loadFile;

  PlateConfig config;

  private final ScheduleFactory scheduleFactory;

  private final GithubService githubService;

  private final DeployService deployService;

  private final Logger logger;

  public Main(
      ScheduleFactory scheduleFactory,
      GithubService githubService,
      DeployService deployService,
      Logger logger) {
    this.scheduleFactory = scheduleFactory;
    this.githubService = githubService;
    this.deployService = deployService;
    this.logger = logger;
  }

  public GithubService getGithubService() {
    return githubService;
  }

  /** メイン処理実行クラス コマンドプロンプトを起動して終了する。 */
  public void execute() {
    Commander commander = new Commander(address, port, this);
    new Thread(commander).start();
  }

  /**
   * userを取得します。
   *
   * @return user
   */
  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  /** パスワード */
  private String cryptPass;

  /** cryptPassを取得します。 */
  public String getCryptPass() {
    return cryptPass;
  }

  /** cryptPassを設定します。 */
  public void setCryptPass(String cryptPass) {
    this.cryptPass = cryptPass;
  }

  /** スタータークラスをスレッドプールを利用して起動する。 */
  public void start(Starter starter) {
    // スターターがスタートしているのかのフラグ
    starter.setStarted(true);
    exec.execute(starter);
  }

  /** キーを追加する。 */
  public void addKey(String key, String className, String port) {
    addKey(key, className, "main", port);
  }

  /** キーを追加する。 */
  public void addKey(String key, String className, String methodName, String port) {
    KeyInfo keyInfo = new KeyInfo(key, className, methodName, this);
    if (portMap.containsKey(port)) {
      portMap.get(port).getList().add(keyInfo);
      keyInfo.setPorter(portMap.get(port));
    } else {
      Porter porter = new Porter(this);
      keyInfo.setPorter(porter);
      porter.getList().add(keyInfo);
      portMap.put(port, porter);
    }
  }

  /** キーを呼び出す。 */
  public void callKey(String port, String key, String[] params) throws CmdException {
    checkPort(port);
    keyAction(key, port, startingKey -> start(startingKey.create(params, StarterKind.CALL)));
  }

  public void shutdownKey(String port, String key, String[] params) throws CmdException {
    checkPort(port);

    keyAction(
        key,
        port,
        startingKey -> {
          var lastStartParams = stopStarters(startingKey, params);
          if (startingKey.getRecovery() == RecoveryMethod.AUTO && startingKey.status.isEnable()) {
            startingKey.build();
            start(startingKey.create(lastStartParams, StarterKind.SERVICE));
          }
        });
  }

  /** recoveryの設定に関わらず、停止して再ビルド・再起動する(デプロイ後の再起動用)。 */
  void restartKey(String port, String key) throws CmdException {
    checkPort(port);
    keyAction(
        key,
        port,
        startingKey -> {
          var lastStartParams = stopStarters(startingKey, new String[0]);
          if (startingKey.status.isEnable()) {
            startingKey.build();
            start(startingKey.create(lastStartParams, StarterKind.SERVICE));
          }
        });
  }

  /** 稼働中のStarterを停止してリストをクリアし、最後に停止したStarterのパラメータを返す。 */
  private String[] stopStarters(KeyInfo startingKey, String[] params) throws CmdException {
    String[] lastStartParams = null;
    for (Starter starter : startingKey.getStarterList()) {
      if (!starter.isFinish()) {
        try {
          if (params.length == 0) {
            starter.shutdown();
          } else {
            starter.shutdown(params);
          }
          if (!starter.isFinish()) {
            // 停止に失敗した場合は、二重起動を防ぐためリストを保持してエラーにする
            throw new CmdException("Starter shutdown failed: " + startingKey.getKey());
          }
          lastStartParams = starter.getParams();
        } catch (CmdException e) {
          throw e;
        } catch (Exception e) {
          throw new CmdException(e);
        }
      }
    }
    startingKey.getStarterList().clear();
    return lastStartParams;
  }

  /** キーを編集する。 */
  public void editKey(String key, String className, String methodName, String port)
      throws CmdException {
    checkPort(port);
    keyAction(
        key,
        port,
        startingKey -> {
          if (className != null) {
            startingKey.setClassName(className);
            if (methodName != null) {
              startingKey.setMethodName(methodName);
            }
          }
        });
  }

  public void renameKey(String key, String newKey, String port) throws CmdException {
    checkPort(port);
    keyAction(key, port, startingKey -> startingKey.setKey(newKey));
  }

  /** キーを除去する。 */
  public void removeKey(String key, String port) throws CmdException {
    checkPort(port);
    keyAction(
        key,
        port,
        startingKey -> {
          // 稼働中のStarterとスケジュールを止めてから除去する
          stopStarters(startingKey, new String[0]);
          if (startingKey.getSchedule() != null) {
            startingKey.getSchedule().cancel();
          }
          startingKey.closeClassLoader();
          portMap.get(port).getList().remove(startingKey);
        });
  }

  /** キーを使用可にする。 */
  public void enableKey(String key, String port) throws CmdException {
    checkPort(port);
    keyAction(key, port, startingKey -> startingKey.setStatus(KeyState.ENABLE));
  }

  /** キーを使用不可にする。 */
  public void disableKey(String key, String port) throws CmdException {
    checkPort(port);
    keyAction(key, port, startingKey -> startingKey.setStatus(KeyState.DISABLE));
  }

  /** キーを自動リカバリーにする。 */
  public void autoKey(String key, String port) throws CmdException {
    checkPort(port);
    keyAction(key, port, startingKey -> startingKey.setRecovery(RecoveryMethod.AUTO));
  }

  /** キーを自動リカバリーにする。 */
  public void manualKey(String key, String port) throws CmdException {
    checkPort(port);
    keyAction(key, port, startingKey -> startingKey.setRecovery(RecoveryMethod.MANUAL));
  }

  PlateConfig loadConfig(File file) {
    try {
      return new Yaml()
          .loadAs(
              new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8),
              PlateConfig.class);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /** 認証なしで設定ファイルをロードする。 */
  public void load(File file) {
    var loaded = loadConfig(file);
    validate(loaded);
    loadFile = file;
    config = loaded;
    init(config);
  }

  /** 設定を検証し、order/method未指定をデフォルト値で補完する。副作用(停止・登録)は行わない。 */
  void validate(PlateConfig config) {
    if (config.service != null && config.service.services != null) {
      config.service.services.forEach(service -> validateStarter("service", service));
    }
    if (config.batch != null && config.batch.batches != null) {
      config.batch.batches.forEach(
          batch -> {
            validateStarter("batch", batch);
            if (batch.schedule != null) {
              try {
                createSchedule(batch.schedule);
              } catch (RuntimeException e) {
                throw new IllegalArgumentException(
                    "batch:" + batch.key + " のスケジュールが不正です:" + e.getMessage(), e);
              }
            }
          });
    }
  }

  private void validateStarter(String type, StarterDto dto) {
    if (dto.key == null || dto.className == null) {
      throw new IllegalArgumentException(type + ":key と className は必須です:" + dto.key);
    }
    if (dto.order == null) {
      dto.order = new OrderDto();
    }
    if (dto.method == null) {
      dto.method = new MethodDto();
    }
    if (dto.method.startup == null) {
      dto.method.startup = "main";
    }
  }

  private Schedule createSchedule(ScheduleDto dto) {
    if (dto.cron != null) {
      return scheduleFactory.create(dto.cron);
    }
    return scheduleFactory.create(dto.minute, dto.hour, dto.day, dto.month, dto.dayOfWeek);
  }

  void init(PlateConfig config) {
    this.user = config.user;
    this.cryptPass = config.hash;
    if (config.service != null) {
      Porter servicePorter = new Porter(this);
      portMap.put("service", servicePorter);
      if (config.service.classPath != null) {
        config.service.classPath.forEach(
            classPath -> {
              CpInfo cpInfo = new CpInfo(classPath);
              servicePorter.addCp(cpInfo);
              cpInfo.setStatus(CpState.INCLUDED);
            });
      }
      if (config.service.services != null) {
        config.service.services.stream()
            .sorted((a, b) -> a.order.startup - b.order.startup)
            .forEach(
                service -> {
                  KeyInfo startKey =
                      new KeyInfo(service.key, service.className, service.method.startup, this);
                  startKey.setPorter(servicePorter);
                  startKey.shutdownMethodName = service.method.shutdown;
                  startKey.statusMethodName = service.method.status;
                  servicePorter.getList().add(startKey);
                  if (service.classPath != null) {
                    service.classPath.forEach(
                        classPath -> {
                          CpInfo cpInfo = new CpInfo(classPath);
                          startKey.addCp(cpInfo);
                          cpInfo.setStatus(CpState.INCLUDED);
                        });
                  }
                  if (!service.disabled) {
                    startKey.setStatus(KeyState.ENABLE);
                  }
                  if (service.recovery) {
                    startKey.setRecovery(RecoveryMethod.AUTO);
                  }
                  startKey.create(service.parameters, StarterKind.SERVICE);
                });
      }

      servicePorter.build();
      servicePorter.getList().stream()
          .filter(keyInfo -> keyInfo.status.isEnable())
          .forEach(
              keyInfo -> {
                for (Starter starter : keyInfo.getStarterList()) {
                  start(starter);
                }
              });
    }
    if (config.batch != null) {
      Porter batchPorter = new Porter(this);
      portMap.put("batch", batchPorter);
      if (config.batch.classPath != null) {
        config.batch.classPath.forEach(
            classPath -> {
              CpInfo cpInfo = new CpInfo(classPath);
              batchPorter.addCp(cpInfo);
              cpInfo.setStatus(CpState.INCLUDED);
            });
      }
      if (config.batch.batches != null) {
        config.batch.batches.stream()
            .sorted((a, b) -> a.order.startup - b.order.startup)
            .forEach(
                batch -> {
                  KeyInfo startKey =
                      new KeyInfo(batch.key, batch.className, batch.method.startup, this);
                  startKey.shutdownMethodName = batch.method.shutdown;
                  startKey.statusMethodName = batch.method.status;
                  startKey.setPorter(batchPorter);
                  batchPorter.getList().add(startKey);
                  if (batch.classPath != null) {
                    batch.classPath.forEach(
                        classPath -> {
                          CpInfo cpInfo = new CpInfo(classPath);
                          startKey.addCp(cpInfo);
                          cpInfo.setStatus(CpState.INCLUDED);
                        });
                  }
                  if (!batch.disabled) {
                    startKey.setStatus(KeyState.ENABLE);
                  }
                  if (batch.schedule != null) {
                    // disabledでも登録し、実行時にstatusを見てskipする(enableで即有効になる)
                    Schedule schedule = createSchedule(batch.schedule);
                    schedule.register(timer, startKey.create(batch.parameters, StarterKind.BATCH));
                    startKey.setSchedule(schedule);
                  }
                });
      }
      batchPorter.build();
    }
    watchReleaseDir(config.release);
  }

  void watchReleaseDir(Map<String, ReleaseDto> releaseDtoMap) {
    if (releaseDtoMap == null) {
      return;
    }
    if (releaseDtoMap.isEmpty()) {
      return;
    }
    if (watchThread != null && watchThread.isAlive()) {
      // 監視スレッドの重複起動を防ぐ
      return;
    }
    var autoReleaseList =
        releaseDtoMap.entrySet().stream()
            .map(entry -> entry.getValue())
            .filter(releaseDto -> releaseDto.auto != null && releaseDto.auto.detect != null)
            .collect(Collectors.toList());
    if (autoReleaseList.isEmpty()) {
      return;
    }
    var watchPathList =
        autoReleaseList.stream().map(releaseDto -> releaseDto.dirPath).distinct().toList();
    Thread thread =
        new Thread(
            () -> {
              try (WatchService service = FileSystems.getDefault().newWatchService()) {
                var watchKeyPathMap = new HashMap<WatchKey, String>();
                for (var watchPath : watchPathList) {
                  var path = Path.of(watchPath);
                  watchKeyPathMap.put(
                      path.register(
                          service,
                          new Kind[] {StandardWatchEventKinds.ENTRY_CREATE},
                          new Modifier[] {}),
                      watchPath);
                }
                WatchKey key = null;
                while (!watchKeyPathMap.isEmpty() && (key = service.take()) != null) {
                  var watchPath = watchKeyPathMap.get(key);
                  if (watchPath == null) continue;
                  for (WatchEvent<?> event : key.pollEvents()) {
                    if (StandardWatchEventKinds.OVERFLOW.equals(event.kind())) continue;
                    // eventではファイル名しかとれない
                    var file = (Path) event.context();
                    try {
                      release(watchPath, file, autoReleaseList);
                    } catch (Throwable t) {
                      stackTrace("release error:" + file, t);
                    }
                  }
                  if (!key.reset()) {
                    logger.warning("監視対象ディレクトリが無効になりました:" + watchPath);
                    watchKeyPathMap.remove(key);
                  }
                }
              } catch (IOException e) {
                stackTrace("release watch error", e);
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
            });
    thread.setDaemon(false); // mainスレッドと運命を共に
    watchThread = thread;
    thread.start();
  }

  void release(String watchPath, Path filePath, List<ReleaseDto> autoReleaseList) {
    var fileName = filePath.toString();
    if (fileName.startsWith("ERR_")) {
      // 失敗済みとしてリネームされたファイルは対象外
      return;
    }
    for (var releaseDto : autoReleaseList) {
      if (!watchPath.equals(releaseDto.dirPath)) {
        continue;
      }
      var pattern = Pattern.compile(releaseDto.auto.detect);
      var matcher = pattern.matcher(fileName);
      if (!matcher.find()) {
        continue;
      }
      var tag = matcher.group(1);
      var file = new File(watchPath, fileName);
      try {
        var result = githubService.download(releaseDto, tag);
        if (result != null) {
          logger.info(result);
        }
      } catch (Exception e) {
        stackTrace("release download error:" + tag, e);
        markError(file);
        continue;
      }
      try {
        deployService.deploy(releaseDto, tag);
      } catch (Exception e) {
        stackTrace("release deploy error:" + tag, e);
        markError(file);
        continue;
      }
      logger.info("release deploy success:" + tag);
      if (releaseDto.auto.service != null) {
        try {
          // recoveryに関わらず再起動する
          restartKey("service", releaseDto.auto.service);
        } catch (Exception e) {
          // デプロイは成功しているためERR_にはリネームしない
          stackTrace("release restart error:" + releaseDto.auto.service, e);
        }
      }
    }
  }

  private void markError(File file) {
    if (!file.renameTo(new File(file.getParent(), "ERR_" + file.getName()))) {
      logger.warning("ERR_へのリネームに失敗しました:" + file.getPath());
    }
  }

  /** 認証ありで設定を読み込む。 */
  public void load(String fileName, String user, String pass) throws CmdException {
    var file = new File(fileName);
    var config = loadConfig(file);
    var cryptPass = Base64.getEncoder().encodeToString(Crypt.encrypt3(user, pass));
    if (!config.user.equals(user) || !config.hash.equals(cryptPass)) {
      throw new CmdException("認証エラー");
    }
    // 検証に失敗した場合は既存の稼働状態を変更しない
    try {
      validate(config);
    } catch (RuntimeException e) {
      stackTrace("設定の検証に失敗しました。既存の状態を維持します", e);
      throw new CmdException("設定エラー:" + e.getMessage());
    }
    // 既存のStarter・スケジュール・監視スレッドを停止してから再初期化する
    stopAll();
    loadFile = file;
    this.config = config;
    try {
      init(config);
    } catch (RuntimeException e) {
      // 中途半端な状態を残さない
      stackTrace("設定の読み込みに失敗しました。読み込み済みの内容を破棄します", e);
      stopAll();
      throw new CmdException("設定の読み込みに失敗しました:" + e.getMessage());
    }
  }

  /** 既存のStarter、スケジュール、監視スレッドを停止して初期状態に戻す。 */
  void stopAll() {
    shutdown();
    timer.cancel();
    timer = new Timer();
    if (watchThread != null) {
      watchThread.interrupt();
      watchThread = null;
    }
    portMap.values().forEach(Porter::closeAllClassLoaders);
    portMap.clear();
  }

  public void save(String user, String pass) throws CmdException {
    save(loadFile, user, pass);
  }

  /** 設定を保存する。 */
  public void save(File file, String user, String pass) throws CmdException {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
      config.user = user;
      config.hash = Base64.getEncoder().encodeToString(Crypt.encrypt3(user, pass));
      new Yaml().dump(config, writer);
    } catch (IOException e) {
      throw new CmdException(e);
    }
  }

  public Map<String, Porter> getPortMap() {
    return portMap;
  }

  /** サーバーを終了する。 */
  public void exit() {
    shutdown();
    System.exit(0);
  }

  /** 全ての実行を自動復帰せずに終了させる. できるかぎりshutdownする. */
  void shutdown() {
    // 設定ファイルのshutdown順序(未設定は最後)
    Map<String, Integer> shutdownOrder = new HashMap<>();
    if (config != null && config.service != null && config.service.services != null) {
      config.service.services.forEach(
          service -> {
            if (service.order != null) {
              shutdownOrder.put(service.key, service.order.shutdown);
            }
          });
    }
    // portMapを基準にするため、addコマンドで追加したキーも対象になる
    for (Porter porter : new ArrayList<>(portMap.values())) {
      porter.getList().stream()
          .sorted(
              Comparator.comparingInt(
                  (KeyInfo keyInfo) ->
                      shutdownOrder.getOrDefault(keyInfo.getKey(), Integer.MAX_VALUE)))
          .forEach(
              keyInfo -> {
                for (Starter starter : new ArrayList<>(keyInfo.getStarterList())) {
                  try {
                    starter.shutdown();
                  } catch (Exception e) {
                    stackTrace("Starter shutdown error", e);
                  }
                }
              });
    }
  }

  /**
   * 指定のポート情報が存在するかをチェックします
   *
   * @param port ポート情報
   * @return 指定のポート情報がない場合はfalse,ある場合はtrue
   */
  public boolean exists(String port) {
    return List.of("service", "batch").contains(port);
  }

  /** キーのクラスパスを追加する。 */
  public void addCp(String key, CpInfo cpInfo, String port) throws CmdException {
    checkPort(port);
    keyAction(key, port, startingKey -> startingKey.addCp(cpInfo));
  }

  /** キーのクラスパスを除去する。 */
  public void removeCp(String key, String iCp, String port) throws CmdException {
    checkPort(port);
    keyAction(key, port, startingKey -> startingKey.removeCp(iCp));
  }

  public void addPortCp(CpInfo cpInfo, String port) throws CmdException {
    checkPort(port);
    portMap.get(port).addCp(cpInfo);
  }

  public void removePortCp(String iCp, String port) throws CmdException {
    checkPort(port);
    portMap.get(port).removeCp(iCp);
  }

  /** 終了したスタートリストを全て削除する。 */
  public void purge() {
    Iterator<Entry<String, Porter>> iterator = portMap.entrySet().iterator();
    List<Starter> removeList = new ArrayList<Starter>();
    while (iterator.hasNext()) {
      Entry<String, Porter> entry = iterator.next();
      for (KeyInfo key : entry.getValue().getList()) {
        for (Starter starter : key.getStarterList()) {
          if (!starter.isAlive()) {
            removeList.add(starter);
          }
        }

        if (removeList.size() > 0) {
          key.getStarterList().removeAll(removeList);
        }
        removeList.clear();
      }
    }
  }

  public File getLoadFile() {
    return loadFile;
  }

  public PlateConfig getConfig() {
    return config;
  }

  public void checkPort(String port) throws CmdException {
    if (!portMap.containsKey(port)) {
      throw new CmdException("ポートが設定されていません.");
    }
  }

  public void keyAction(String key, String port, ThrowingConsumer<KeyInfo, CmdException> consumer)
      throws CmdException {
    for (KeyInfo startingKey : portMap.get(port).getList()) {
      if (startingKey.getKey().equals(key)) {
        consumer.accept(startingKey);
        return;
      }
    }
    throw new CmdException("キーが設定されていません.");
  }

  public void stackTrace(String message, Throwable t) {
    logger.log(Level.SEVERE, message, t);
  }

  public void info(String message) {
    logger.info(message);
  }
}
