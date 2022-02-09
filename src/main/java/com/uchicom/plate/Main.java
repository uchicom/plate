// (C) 2012 uchicom
package com.uchicom.plate;

import com.uchicom.plate.util.Base64;
import com.uchicom.plate.util.Crypt;
import com.uchicom.util.Parameter;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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

/**
 * plateサーバーのメインクラス 全てを同一のスレッドプールで管理する。メインクラスは
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

    if (parameter.is("file")) {

    } else {

    }
    if (args.length > 0) {
      // コマンドプロンプトのポートとデフォルトの設定ファイルを読み込む
      String[] addresses = args[0].split(":");
      if (addresses.length == 2) {
        plate = new Main(addresses[0], Integer.parseInt(addresses[1]));
      } else if (addresses.length == 1) {
        plate = new Main(Integer.parseInt(args[0]));
      }
    } else {
      plate = new Main();
    }
    if (args.length > 1) {
      // 設定ファイルロード処理
      plate.load(args[1]);
    }
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

  /** ポート情報保持リスト */
  // private Map<String, Porter> porterMap = new HashMap<String, Porter>();

  /** ユーザー名 */
  private String user;

  private String loadFile;

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
    // セキュリティーマネージャーの設定
    setSecurity();
  }

  /** セキュリティマネージャーでexitをSecurityExceptionにする。 */
  protected void setSecurity() {
    System.setSecurityManager(
        new SecurityManager() {
          /** */
          public void checkPermission(Permission perm) {
            if ("setSecurityManager".equals(perm.getName())) {
              throw new SecurityException("setSecurityManager is no permission.");
            }
          }

          /** */
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
    exec.execute(starter);
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
      Porter porter = new Porter(port, this);
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
          Starter starter = startingKey.create(params, Starter.CMD);
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

  /**
   * 認証なしで設定ファイルをロードする。
   *
   * @param fileName
   * @return
   */
  public boolean load(String fileName) {
    BufferedReader br = null;
    try {
      loadFile = fileName;
      br = new BufferedReader(new InputStreamReader(new FileInputStream(loadFile)));
      String line = br.readLine();
      Porter tmpPorter = null;
      // パスワードチェック
      String[] params = line.trim().split(" ");
      if (params.length == 2) {
        // ユーザー名をセット
        this.user = params[0];
        // パスワードをセット
        this.cryptPass = params[1];
        line = br.readLine();
        while (line != null) {
          params = line.trim().split(" ");
          if (line.startsWith("   ")) {
            // スタートパラメータ一覧
            KeyInfo keyInfo = tmpPorter.getList().get(tmpPorter.getList().size() - 1);
            if (params.length == 1 && "".equals(params[0])) {
              keyInfo.create(new String[0], Starter.INIT);
            } else {
              keyInfo.create(params, Starter.INIT);
            }
          } else if (line.startsWith("  ")) {
            // クラスパス(キーまたはポートの)
            if (tmpPorter.getList().size() > 0) {
              // キーのクラスパス(インクルードエクスクルード)
              CpInfo cpInfo = new CpInfo(params[0]);
              tmpPorter.getList().get(tmpPorter.getList().size() - 1).addCp(cpInfo);
              if (params.length > 2 && params[1].equals("I")) {
                cpInfo.setStatus(CpInfo.STATUS_INCLUDED);
              }
            } else {
              // ポートのクラスパス(インクルードエクスクルード)
              CpInfo cpInfo = new CpInfo(params[0]);
              tmpPorter.addCp(cpInfo);
              if (params.length > 2 && params[1].equals("I")) {
                cpInfo.setStatus(CpInfo.STATUS_INCLUDED);
              }
            }
          } else if (line.startsWith(" ")) {
            // キー(イネーブルディスエーブル)
            KeyInfo startKey = new KeyInfo(params[0], params[1], params[2]);
            startKey.setPorter(tmpPorter);
            tmpPorter.getList().add(startKey);
            if (params.length > 3 && "E".equals(params[3])) {
              startKey.setStatus(KeyInfo.STATUS_ENABLE);
            }
            if (params.length > 4 && "A".equals(params[4])) {
              startKey.setRecovery(KeyInfo.AUTO);
            }
          } else {
            // ポート(オープンクローズ)
            tmpPorter = new Porter(params[0], this);
            portMap.put(params[0], tmpPorter);
            if (params.length > 1 && "O".equals(params[1])) {
              tmpPorter.setStatus(Porter.STATUS_OPEN);
            }
          }
          line = br.readLine();
        }

        // ポートのオープン処理
        Iterator<Entry<String, Porter>> ite = portMap.entrySet().iterator();
        while (ite.hasNext()) {
          Entry<String, Porter> ent = ite.next();
          build(ent.getKey());
          if (ent.getValue().getStatus() == Porter.STATUS_OPEN) {
            openPort(ent.getKey(), false);
          }
          // スターターの起動処理
          for (KeyInfo keyInfo : ent.getValue().getList()) {
            for (Starter starter : keyInfo.getStarterList()) {
              start(starter);
            }
          }
        }

      } else {
        br.close();
        return false;
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return true;
  }

  /**
   * 認証ありで設定を読み込む。
   *
   * @param fileName
   * @return
   */
  public boolean load(String fileName, String user, String pass) {
    BufferedReader br = null;
    try {
      br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
      String line = br.readLine();
      Porter tmpPorter = null;
      // パスワードチェック
      String[] params = line.trim().split(" ");
      String cryptPass = Base64.encode(Crypt.encrypt3(user, pass));
      if (params.length == 2 && params[0].equals(user) && params[1].equals(cryptPass)) {
        // ユーザー名をセット
        this.user = user;
        // パスワードをセット
        this.cryptPass = cryptPass;
        line = br.readLine();
        while (line != null && !"".equals(line.trim())) {
          params = line.trim().split(" ");
          if (line.startsWith("  ")) {
            // クラスパス(キーまたはポートの)
            if (tmpPorter.getList().size() > 0) {
              // キーのクラスパス(インクルードエクスクルード)
              CpInfo cpInfo = new CpInfo(params[0]);
              tmpPorter.getList().get(tmpPorter.getList().size() - 1).addCp(cpInfo);
              if (params.length > 2 && params[1].equals("I")) {
                cpInfo.setStatus(CpInfo.STATUS_INCLUDED);
              }
            } else {
              // ポートのクラスパス(インクルードエクスクルード)
              CpInfo cpInfo = new CpInfo(params[0]);
              tmpPorter.addCp(cpInfo);
              if (params.length > 2 && params[1].equals("I")) {
                cpInfo.setStatus(CpInfo.STATUS_INCLUDED);
              }
            }
          } else if (line.startsWith(" ")) {
            // キー(イネーブル:ディスエーブル,リカバリーオートマニュアル)
            KeyInfo startKey = new KeyInfo(params[0], params[1], params[2]);
            startKey.setPorter(tmpPorter);
            tmpPorter.getList().add(startKey);
            if (params.length > 3 && "E".equals(params[3])) {
              startKey.setStatus(KeyInfo.STATUS_ENABLE);
            }
            if (params.length > 4 && "A".equals(params[4])) {
              startKey.setRecovery(KeyInfo.AUTO);
            }
          } else {
            // ポート(オープンクローズ)
            tmpPorter = new Porter(params[0], this);
            portMap.put(params[0], tmpPorter);
            if (params.length > 1 && "O".equals(params[1])) {
              tmpPorter.setStatus(Porter.STATUS_OPEN);
            }
          }
          line = br.readLine();
        }

        // ポートのオープン処理
        Iterator<Entry<String, Porter>> ite = portMap.entrySet().iterator();
        while (ite.hasNext()) {
          Entry<String, Porter> ent = ite.next();
          build(ent.getKey());
          if (ent.getValue().getStatus() == Porter.STATUS_OPEN) {
            openPort(ent.getKey(), false);
          }
        }
      } else {
        br.close();
        return false;
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return true;
  }

  public boolean save(String user, String pass) {
    return save(loadFile, user, pass);
  }
  /**
   * 設定を保存する。
   *
   * @param fileName
   * @return
   */
  public boolean save(String fileName, String user, String pass) {

    try (FileOutputStream fos = new FileOutputStream(fileName); ) {
      fos.write(user.getBytes());
      fos.write(' ');
      fos.write(Base64.encode(Crypt.encrypt3(user, pass)).getBytes());
      fos.write("\r\n".getBytes());
      fos.flush();
      Set<Entry<String, Porter>> set = portMap.entrySet();
      Iterator<Entry<String, Porter>> ite = set.iterator();
      while (ite.hasNext()) {
        Entry<String, Porter> entry = ite.next();
        // ポート情報
        Porter porter = entry.getValue();
        fos.write(porter.getPort().getBytes());
        fos.write(' ');
        fos.write(porter.getStatus() == Porter.STATUS_CLOSE ? 'C' : 'O');
        fos.write("\r\n".getBytes());
        fos.flush();
        // ポートクラスパス情報
        for (CpInfo cpInfo : porter.getCpList()) {
          fos.write("  ".getBytes());
          fos.write(cpInfo.getUrl().toString().getBytes());
          fos.write(' ');
          fos.write(cpInfo.getStatus() == CpInfo.STATUS_EXCLUDED ? 'E' : 'I');
          fos.write("\r\n".getBytes());
          fos.flush();
        }
        // 別名情報
        for (KeyInfo startKey : porter.getList()) {
          fos.write(' ');
          fos.write(startKey.getKey().getBytes());
          fos.write(' ');
          fos.write(startKey.getClassName().getBytes());
          fos.write(' ');
          fos.write(startKey.getMethodName().getBytes());
          fos.write(' ');
          fos.write(startKey.getStatus() == KeyInfo.STATUS_DISABLE ? 'D' : 'E');
          fos.write(' ');
          fos.write(startKey.getRecovery() == KeyInfo.AUTO ? 'A' : 'M');
          fos.write("\r\n".getBytes());
          fos.flush();
          // 別名クラスパス情報
          for (CpInfo cpInfo : startKey.getCpList()) {
            fos.write("  ".getBytes());
            fos.write(cpInfo.getUrl().toString().getBytes());
            fos.write(' ');
            fos.write(cpInfo.getStatus() == CpInfo.STATUS_EXCLUDED ? 'E' : 'I');
            fos.write("\r\n".getBytes());
            fos.flush();
          }
          // 起動情報(aliveのものを保存する)
          for (Starter starter : startKey.getStarterList()) {
            if (starter.isAlive()) {
              fos.write("   ".getBytes());
              for (String param : starter.getParams()) {
                fos.write(param.getBytes());
                fos.write(" ".getBytes());
              }
              fos.write("\r\n".getBytes());
              fos.flush();
            }
          }
        }
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return true;
  }

  /**
   * ポートをオープンする。
   *
   * @param port
   * @return
   */
  public boolean openPort(String port, boolean openCheck) {
    if (portMap.containsKey(port)) {
      Porter porter = portMap.get(port);
      if (!openCheck || porter.getStatus() == Porter.STATUS_CLOSE) {
        new Thread(porter).start();
        portMap.get(port).setStatus(Porter.STATUS_OPEN);
        // porterMap.put(port, porter);
        return true;
      }
    }
    return false;
  }

  /**
   * ポートを閉じる。
   *
   * @param port
   * @return
   */
  public boolean closePort(String port, boolean closeCheck) {
    if (portMap.containsKey(port)) {
      Porter portInfo = portMap.get(port);
      if (!closeCheck || portInfo.getStatus() == Porter.STATUS_OPEN) {
        // porterMap.get(port).close();
        portInfo.close();
        portInfo.setStatus(Porter.STATUS_CLOSE);
        return true;
      }
    }
    return false;
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
   * 指定のポート情報を作成します。
   *
   * @param port
   * @return
   */
  public boolean createPort(String port) {
    if (!portMap.containsKey(port)) {
      Porter porter = new Porter(port, this);
      portMap.put(port, porter);
      return true;
    }
    return false;
  }

  /**
   * 指定のポート情報が存在するかをチェックします
   *
   * @param port
   * @return 指定のポート情報がない場合はfalse,ある場合はtrue
   */
  public boolean exists(String port) {
    return portMap.containsKey(port);
  }

  /**
   * 指定のポート情報が存在するかをチェックします
   *
   * @param port
   * @return 指定のポート情報がない場合はfalse,ある場合はtrue
   */
  public boolean exists(String port, String key) {
    return portMap.containsKey(port) && portMap.get(port).getList().contains(new KeyInfo(key));
  }

  /**
   * 指定のポート情報を削除します。
   *
   * @param port
   * @return 削除した場合はtrue, 削除しなかった場合はfalse
   */
  public boolean dropPort(String port) {
    if (portMap.containsKey(port)) {
      portMap.remove(port);
      return true;
    }
    return false;
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

  public String getLoadFile() {
    return loadFile;
  }
}
