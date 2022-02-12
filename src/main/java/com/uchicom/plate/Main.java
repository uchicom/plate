// (C) 2012 uchicom
package com.uchicom.plate;

import com.uchicom.plate.Starter.StarterKind;
import com.uchicom.plate.dto.PlateConfig;
import com.uchicom.plate.scheduler.Schedule;
import com.uchicom.plate.scheduler.ScheduleFactory;
import com.uchicom.plate.scheduler.cron.CronParser;
import com.uchicom.plate.util.Base64;
import com.uchicom.plate.util.Crypt;
import com.uchicom.util.Parameter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.Permission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.yaml.snakeyaml.Yaml;

/**
 * serverとbatchを管理するミドルウェア.
 *
 * @author Uchiyama Shigeki
 */
public class Main {

  /** コマンドプロンプトのデフォルトポート */
  public static final int DEFAULT_PORT = 8124;

  public static final int DEFAULT_POOL_SIZE = 11;

  public static final String DEFAULT_ADDRESS = "localhost";

  public Timer timer = new Timer();

  /**
   * コマンド引数を使用してサーバーを起動する。 ポート番号を指定してコマンドを受け付けるポート番号を指定する。
   * 任意でデフォルトの設定ファイル名を指定する。指定されている場合は設定ファイルをロードする。
   *
   * @param args
   */
  public static void main(String[] args) {
    Parameter parameter = new Parameter(args);
    Main plate = null;

    if (parameter.is("port")) {
      if (parameter.is("host")) {
        plate = new Main(parameter.get("host"), parameter.getInt("port"));
      } else {
        plate = new Main(parameter.getInt("port"));
      }
    } else {
      plate = new Main();
    }
    if (parameter.is("file")) {
      // 設定ファイルロード処理
      plate.load(parameter.getFile("file"));
    }

    // セキュリティーマネージャーの設定
    plate.setSecurity();
    Runtime.getRuntime().addShutdownHook(new ShutdownHook(plate));
    plate.execute();
  }

  /** スレッドプール */
  ExecutorService exec;

  /** コンソール待ちうけアドレス */
  private String address;

  /** ポート */
  private int port;

  /** plateの状態 */
  private int plateStatus;

  /** キー情報保持マップ */
  private Map<String, Porter> portMap = new HashMap<String, Porter>();

  /** ユーザー名 */
  private String user;

  private File loadFile;

  PlateConfig config;

  ScheduleFactory scheduleFactory = new ScheduleFactory(new CronParser());

  /**
   * userを取得します。
   *
   * @return user
   */
  public String getUser() {
    return user;
  }

  /**
   * userを設定します。
   *
   * @param user
   */
  public void setUser(String user) {
    this.user = user;
  }

  /** パスワード */
  private String cryptPass;

  /**
   * cryptPassを取得します。
   *
   * @return cryptPass
   */
  public String getCryptPass() {
    return cryptPass;
  }

  /**
   * cryptPassを設定します。
   *
   * @param cryptPass
   */
  public void setCryptPass(String cryptPass) {
    this.cryptPass = cryptPass;
  }

  /** 引数なしのコンストラクタ。 ポートはデフォルトポートで起動する。 */
  public Main() {
    this(DEFAULT_PORT);
  }

  /**
   * スレッドプールは適宜拡張されたり減ったりする。 呼ばれていない間にメモリが使えなくなる可能性があるので、 使用頻度が少ない場合はメモリを使用しない。
   *
   * @param port
   */
  private Main(int port) {
    this(port, DEFAULT_POOL_SIZE);
  }

  private Main(String address, int port) {
    this(address, port, DEFAULT_POOL_SIZE);
  }

  private Main(int port, int pool) {
    this(DEFAULT_ADDRESS, port, pool);
  }

  private Main(String address, int port, int pool) {
    this.address = address;
    this.port = port;
    exec = Executors.newFixedThreadPool(pool);
  }

  /** セキュリティマネージャーでexitをSecurityExceptionにする。 */
  protected void setSecurity() {
    System.setSecurityManager(
        new SecurityManager() {
          @Override
          public void checkPermission(Permission perm) {
            if ("setSecurityManager".equals(perm.getName())) {
              throw new SecurityException("setSecurityManager is no permission.");
            }
          }

          @Override
          public void checkExit(int status) {
            if (plateStatus == 0) {
              throw new SecurityException("exit is no permission.");
            }
          }
        });
  }

  /** メイン処理実行クラス コマンドプロンプトを起動して終了する。 */
  public void execute() {
    Commander commander = new Commander(address, port, this);
    new Thread(commander).start();
  }

  /**
   * スタータークラスをスレッドプールを利用して起動する。
   *
   * @param starter
   */
  public void start(Starter starter) {
    // スターターがスタートしているのかのフラグ
    starter.setStarted(true);
    exec.execute(
        () -> {
          try {
            starter.run();
          } catch (Throwable t) {
            t.printStackTrace();
          }
        });
  }

  /**
   * キーを追加する。
   *
   * @param key
   * @param className
   * @param port
   * @return
   */
  public boolean addKey(String key, String className, String port) {
    return addKey(key, className, "main", port);
  }

  /**
   * キーを追加する。
   *
   * @param key
   * @param className
   * @param methodName
   * @param port
   * @return
   */
  public boolean addKey(String key, String className, String methodName, String port) {
    KeyInfo keyInfo = new KeyInfo(key, className, methodName);
    if (portMap.containsKey(port)) {
      portMap.get(port).getList().add(keyInfo);
      keyInfo.setPorter(portMap.get(port));
    } else {
      Porter porter = new Porter(this);
      keyInfo.setPorter(porter);
      porter.getList().add(keyInfo);
      portMap.put(port, porter);
    }
    return true;
  }

  /**
   * キーを呼び出す。
   *
   * @param port
   * @param key
   * @param params
   * @return
   */
  public boolean callKey(String port, String key, String[] params) {
    if (portMap.containsKey(port)) {
      for (KeyInfo startingKey : portMap.get(port).getList()) {
        if (startingKey.getKey().equals(key)) {
          Starter starter = startingKey.create(params, StarterKind.CALL);
          start(starter);
          return true;
        }
      }
    }
    return false;
  }

  /**
   * @param port
   * @param key
   * @param params
   * @return
   */
  public boolean shutdownKey(String port, String key, String[] params) {
    if (portMap.containsKey(port)) {
      for (KeyInfo startingKey : portMap.get(port).getList()) {
        if (startingKey.getKey().equals(key)) {
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
                e.printStackTrace();
              }
            }
          }
          // 再起動処理
          for (Starter starter : startingKey.getStarterList()) {
            if (!starter.isFinish() && starter.getStartingKey().getRecovery() == KeyInfo.AUTO) {
              starter.setRecoveryCount(starter.getRecoveryCount() + 1);
              start(starter);
            } else {
              starter.setFinish(true);
            }
          }
          return true;
        }
      }
    }
    return false;
  }

  /**
   * キーを編集する。
   *
   * @return
   */
  public boolean editKey(String key, String className, String methodName, String port) {
    if (portMap.containsKey(port)) {
      List<KeyInfo> list = portMap.get(port).getList();
      for (KeyInfo startKey : list) {
        if (startKey.getKey().equals(key)) {
          if (className != null) {
            startKey.setClassName(className);
            if (methodName != null) {
              startKey.setMethodName(methodName);
            }
          }

          return true;
        }
      }
    }
    return false;
  }

  public boolean renameKey(String key, String newKey, String port) {
    if (portMap.containsKey(port)) {
      List<KeyInfo> list = portMap.get(port).getList();
      for (KeyInfo startKey : list) {
        if (startKey.getKey().equals(key)) {
          startKey.setKey(newKey);
          return true;
        }
      }
    }
    return false;
  }

  /**
   * キーを除去する。
   *
   * @param key
   * @param port
   * @return
   */
  public boolean removeKey(String key, String port) {
    if (portMap.containsKey(port)) {
      List<KeyInfo> list = portMap.get(port).getList();
      list.remove(new KeyInfo(key));
      return true;
    } else {
      return false;
    }
  }

  /**
   * キーを使用可にする。
   *
   * @param key
   * @param port
   * @return
   */
  public boolean enableKey(String key, String port) {
    if (portMap.containsKey(port)) {
      for (KeyInfo startingKey : portMap.get(port).getList()) {
        if (startingKey.getKey().equals(key)) {
          startingKey.setStatus(KeyInfo.STATUS_ENABLE);
          return true;
        }
      }
    }
    return false;
  }

  /**
   * キーを使用不可にする。
   *
   * @param key
   * @param port
   * @return
   */
  public boolean disableKey(String key, String port) {
    if (portMap.containsKey(port)) {
      for (KeyInfo startingKey : portMap.get(port).getList()) {
        if (startingKey.getKey().equals(key)) {
          startingKey.setStatus(KeyInfo.STATUS_DISABLE);
          return true;
        }
      }
    }
    return false;
  }

  /**
   * キーを自動リカバリーにする。
   *
   * @param key
   * @param port
   * @return
   */
  public boolean autoKey(String key, String port) {
    if (portMap.containsKey(port)) {
      for (KeyInfo startingKey : portMap.get(port).getList()) {
        if (startingKey.getKey().equals(key)) {
          startingKey.setRecovery(KeyInfo.AUTO);
          return true;
        }
      }
    }
    return false;
  }

  /**
   * キーを自動リカバリーにする。
   *
   * @param key
   * @param port
   * @return
   */
  public boolean manualKey(String key, String port) {
    if (portMap.containsKey(port)) {
      for (KeyInfo startingKey : portMap.get(port).getList()) {
        if (startingKey.getKey().equals(key)) {
          startingKey.setRecovery(KeyInfo.MANUAL);
          return true;
        }
      }
    }
    return false;
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
  /**
   * 認証なしで設定ファイルをロードする。
   *
   * @param fileName
   * @return
   */
  public boolean load(File file) {
    loadFile = file;
    config = loadConfig(file);
    init(config);
    return true;
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
              cpInfo.setStatus(CpInfo.STATUS_INCLUDED);
            });
      }
      if (config.service.services != null) {
        config.service.services.forEach(
            service -> {
              KeyInfo startKey = new KeyInfo(service.key, service.className, service.startMethod);
              startKey.setPorter(servicePorter);
              servicePorter.getList().add(startKey);
              if (service.classPath != null) {
                service.classPath.forEach(
                    classPath -> {
                      CpInfo cpInfo = new CpInfo(classPath);
                      startKey.addCp(cpInfo);
                      cpInfo.setStatus(CpInfo.STATUS_INCLUDED);
                    });
              }
              if (!service.disabled) {
                startKey.setStatus(KeyInfo.STATUS_ENABLE);
              }
              if (service.recovery) {
                startKey.setRecovery(KeyInfo.AUTO);
              }
              startKey.create(service.parameters, StarterKind.SERVICE);
            });
      }

      servicePorter.build();
      servicePorter
          .getList()
          .forEach(
              keyInfo -> {
                for (Starter starter : keyInfo.getStarterList()) {
                  start(starter);
                }
              });
    }
    if (config.batch != null) {
      Porter batchPorter = new Porter(this);
      portMap.put("9900", batchPorter);
      if (config.batch.classPath != null) {
        config.batch.classPath.forEach(
            classPath -> {
              CpInfo cpInfo = new CpInfo(classPath);
              batchPorter.addCp(cpInfo);
              cpInfo.setStatus(CpInfo.STATUS_INCLUDED);
            });
      }
      if (config.batch.batches != null) {
        config.batch.batches.forEach(
            batch -> {
              KeyInfo startKey = new KeyInfo(batch.key, batch.className, batch.startMethod);
              startKey.setPorter(batchPorter);
              batchPorter.getList().add(startKey);
              if (batch.classPath != null) {
                batch.classPath.forEach(
                    classPath -> {
                      CpInfo cpInfo = new CpInfo(classPath);
                      startKey.addCp(cpInfo);
                      cpInfo.setStatus(CpInfo.STATUS_INCLUDED);
                    });
              }
              if (!batch.disabled) {
                startKey.setStatus(KeyInfo.STATUS_ENABLE);
              }
              if (batch.schedule != null) {
                Schedule schedule = scheduleFactory.create(batch.schedule.cron);
                schedule.register(timer, startKey.create(batch.parameters, StarterKind.BATCH));
              }
            });
      }
      batchPorter.build();
    }
  }

  /**
   * 認証ありで設定を読み込む。
   *
   * @param fileName
   * @return
   */
  public boolean load(String fileName, String user, String pass) {
    PlateConfig config = loadConfig(loadFile);
    String cryptPass = Base64.encode(Crypt.encrypt3(user, pass));
    if (config.user.equals(user) && config.hash.equals(cryptPass)) {
      init(config);
      loadFile = new File(fileName);
      return true;
    }
    return false;
  }

  public boolean save(String user, String pass) {
    return save(loadFile, user, pass);
  }
  /**
   * 設定を保存する。
   *
   * @param fileName
   * @return 成功した場合はtrue,それ以外はfalse
   */
  public boolean save(File file, String user, String pass) {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
      config.user = user;
      config.hash = Base64.encode(Crypt.encrypt3(user, pass));
      new Yaml().dump(config, writer);
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  /**
   * portMapを取得します。
   *
   * @return portMap
   */
  public Map<String, Porter> getPortMap() {
    return portMap;
  }

  /**
   * portMapを設定します。
   *
   * @param portMap
   */
  public void setPortMap(Map<String, Porter> portMap) {
    this.portMap = portMap;
  }

  /** サーバーを終了する。 */
  public void exit() {
    this.plateStatus = 1;
    System.exit(0);
  }

  /** 全ての実行を自動復帰せずに終了させる. できるかぎりshutdownする. */
  public void shutdown() {
    Set<Entry<String, Porter>> set = portMap.entrySet();
    Iterator<Entry<String, Porter>> iterator = set.iterator();
    while (iterator.hasNext()) {
      Entry<String, Porter> entry = iterator.next();
      for (KeyInfo key : entry.getValue().getList()) {
        for (Starter starter : key.getStarterList()) {
          if (!starter.isFinish()) {
            try {
              starter.shutdown();
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        }
      }
    }
  }

  /**
   * 指定のポート情報が存在するかをチェックします
   *
   * @param port
   * @return 指定のポート情報がない場合はfalse,ある場合はtrue
   */
  public boolean exists(String port) {
    return List.of("service", "batch").contains(port);
  }

  /**
   * キーのクラスパスを追加する。
   *
   * @param key
   * @param classPath
   * @param port
   * @return
   */
  public boolean addCp(String key, CpInfo cpInfo, String port) {
    if (portMap.containsKey(port)) {
      Porter porter = portMap.get(port);
      List<KeyInfo> keyList = porter.getList();
      int iMaxList = keyList.size();
      for (int iList = 0; iList < iMaxList; iList++) {
        KeyInfo startingKey = keyList.get(iList);
        if (startingKey.getKey().equals(key)) {
          try {
            startingKey.addCp(cpInfo);
            return true;
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    }
    return false;
  }

  /**
   * @param key
   * @param protocol
   * @param host
   * @param file
   * @param port
   * @return
   */
  public boolean addCp(String key, String protocol, String host, String file, String port) {
    if (portMap.containsKey(port)) {
      Porter porter = portMap.get(port);
      List<KeyInfo> keyList = porter.getList();
      int iMaxList = keyList.size();
      for (int iList = 0; iList < iMaxList; iList++) {
        KeyInfo startingKey = keyList.get(iList);
        if (startingKey.getKey().equals(key)) {
          try {
            startingKey.addCp(protocol, host, file);
            return true;
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    }
    return false;
  }

  /**
   * キーのクラスパスを除去する。
   *
   * @param key
   * @param iCp
   * @param port
   * @return
   */
  public boolean removeCp(String key, String iCp, String port) {
    if (portMap.containsKey(port)) {
      Porter porter = portMap.get(port);
      List<KeyInfo> keyList = porter.getList();
      int iMaxList = keyList.size();
      for (int iList = 0; iList < iMaxList; iList++) {
        KeyInfo startingKey = keyList.get(iList);
        if (startingKey.getKey().equals(key)) {
          try {
            startingKey.removeCp(iCp);
            return true;
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    }
    return false;
  }

  /**
   * ポートのクラスパスを追加する。
   *
   * @param classPath
   * @param port
   * @return
   * @throws IOException
   */
  public boolean addPortCp(CpInfo cpInfo, String port) {
    if (portMap.containsKey(port)) {
      Porter porter = portMap.get(port);
      porter.addCp(cpInfo);
      return true;
    }
    return false;
  }

  public boolean addPortCp(String protocol, String host, String file, String port)
      throws IOException {
    if (portMap.containsKey(port)) {
      Porter porter = portMap.get(port);
      porter.addCp(protocol, host, file);
      return true;
    }
    return false;
  }

  /**
   * ポートのクラスパスを除去する。
   *
   * @param iCp
   * @param port
   * @return
   */
  public boolean removePortCp(String iCp, String port) {
    if (portMap.containsKey(port)) {
      Porter porter = portMap.get(port);
      porter.removeCp(iCp);
      return true;
    }
    return false;
  }

  /**
   * CPでURLクラスローダーを用意する
   *
   * @param port
   * @return
   */
  public boolean build(String port) {
    if (portMap.containsKey(port)) {
      Porter porter = portMap.get(port);
      porter.build();
      return true;
    }
    return false;
  }

  /**
   * load中
   *
   * @param port
   * @return
   */
  public boolean initBuild(String port) {
    if (portMap.containsKey(port)) {
      Porter porter = portMap.get(port);
      porter.initBuild();
      return true;
    }
    return false;
  }

  /**
   * 終了したスタートリストを全て削除する。
   *
   * @return
   */
  public boolean purge() {
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
    return true;
  }

  public File getLoadFile() {
    return loadFile;
  }
}
