// (C) 2012 uchicom
package com.uchicom.plate;

import com.uchicom.plate.Starter.StarterKind;
import com.uchicom.plate.dto.PlateConfig;
import com.uchicom.plate.enumeration.CpState;
import com.uchicom.plate.enumeration.KeyState;
import com.uchicom.plate.enumeration.RecoveryMethod;
import com.uchicom.plate.exception.CmdException;
import com.uchicom.plate.factory.di.DIFactory;
import com.uchicom.plate.scheduler.Schedule;
import com.uchicom.plate.scheduler.ScheduleFactory;
import com.uchicom.plate.util.Crypt;
import com.uchicom.util.Parameter;
import com.uchicom.util.ThrowingConsumer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
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
import org.yaml.snakeyaml.Yaml;

/**
 * serverとbatchを管理するミドルウェア.
 *
 * @author Uchiyama Shigeki
 */
public class Main {

  public Timer timer = new Timer();

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

  private final Logger logger;

  public Main(ScheduleFactory scheduleFactory, Logger logger) {
    this.scheduleFactory = scheduleFactory;
    this.logger = logger;
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
    exec.execute(
        () -> {
          try {
            starter.run();
          } catch (Throwable t) {
            stackTrace("Starter start error", t);
          }
        });
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
    keyAction(
        key,
        port,
        startingKey -> {
          Starter starter = startingKey.create(params, StarterKind.CALL);
          start(starter);
        });
  }

  public void shutdownKey(String port, String key, String[] params) throws CmdException {
    checkPort(port);

    keyAction(
        key,
        port,
        startingKey -> {
          // 停止処理
          for (Starter starter : startingKey.getStarterList()) {
            if (!starter.isFinish()) {
              try {
                if (params.length == 0) {
                  starter.shutdown();
                } else {
                  starter.shutdown(params);
                }
              } catch (Exception e) {
                throw new CmdException(e);
              }
            }
          }
          // 再起動処理
          for (var starter : startingKey.getStarterList()) {
            if (!starter.isFinish()
                && starter.getStartingKey().getRecovery() == RecoveryMethod.AUTO
                && starter.getStartingKey().status.isEnable()) {
              starter.incrementRecovery();
              start(starter);
            } else {
              starter.setFinish(true);
            }
          }
        });
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
    List<KeyInfo> list = portMap.get(port).getList();
    list.remove(new KeyInfo(key));
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
    loadFile = file;
    config = loadConfig(file);
    init(config);
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
                    Schedule schedule = null;
                    if (batch.schedule.cron != null) {
                      schedule = scheduleFactory.create(batch.schedule.cron);
                    } else {
                      schedule =
                          scheduleFactory.create(
                              batch.schedule.minute,
                              batch.schedule.hour,
                              batch.schedule.day,
                              batch.schedule.month,
                              batch.schedule.dayOfWeek);
                    }
                    schedule.register(timer, startKey.create(batch.parameters, StarterKind.BATCH));
                    startKey.setSchedule(schedule);
                  }
                });
      }
      batchPorter.build();
    }
  }

  /** 認証ありで設定を読み込む。 */
  public void load(String fileName, String user, String pass) throws CmdException {
    var config = loadConfig(loadFile);
    var cryptPass = Base64.getEncoder().encodeToString(Crypt.encrypt3(user, pass));
    if (!config.user.equals(user) || !config.hash.equals(cryptPass)) {
      throw new CmdException("認証エラー");
    }
    init(config);
    loadFile = new File(fileName);
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
    if (config.service == null) {
      return;
    }
    if (config.service.services == null) {
      return;
    }
    config.service.services.stream()
        .sorted((a, b) -> a.order.shutdown - b.order.shutdown)
        .forEach(
            service -> {
              try {
                portMap.get("service").getList().stream()
                    .filter(keyInfo -> keyInfo.getKey().equals(service.key))
                    .forEach(keyInfo -> keyInfo.getStarterList().forEach(Starter::shutdown));
              } catch (Exception e) {
                stackTrace("Starter shutdown error", e);
              }
            });
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
    logger.log(Level.SEVERE, message, 5);
  }

  public void info(String message) {
    logger.info(message);
  }
}
