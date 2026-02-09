// (C) 2012 uchicom
package com.uchicom.plate.cmd;

import com.uchicom.plate.Commander;
import com.uchicom.plate.exception.CmdException;
import com.uchicom.plate.handler.CmdSocketHandler;
import com.uchicom.plate.util.Crypt;
import java.util.Base64;

/**
 * コマンドの抽象クラス.
 *
 * @author Uchiyama Shigeki
 */
public abstract class AbstractCmd {

  /** コマンドを束ねるコマンダークラス */
  protected Commander broker;

  protected String name;

  /** コマンダーを保持するコンストラクタ。 */
  public AbstractCmd(String name, Commander broker) {
    this.broker = broker;
    this.name = name;
  }

  /**
   * コマンドのメイン処理
   *
   * @return メッセージ出力する場合に設定
   */
  public abstract String execute(CmdSocketHandler handler, String[] params) throws CmdException;

  /**
   * 各コマンドで認証チェックをする。
   *
   * @return 認証の結果OKの場合はtrue,それ以外はfalse
   */
  public boolean checkAuth(CmdSocketHandler handler) {
    return (broker.getMain().getUser() == null
            && handler.getUser() != null
            && handler.getPass() != null)
        || (handler.getUser() != null
            && handler.getUser().equals(broker.getMain().getUser())
            && (handler.getPass() != null
                && Base64.getEncoder()
                    .encodeToString(Crypt.encrypt3(handler.getUser(), handler.getPass()))
                    .equals(broker.getMain().getCryptPass())));
  }

  /**
   * 各コマンド実行時にパラメータのチェックを行う。
   *
   * @return パラメータチェックの結果OKの場合はtrue,それ以外はfalse
   */
  public boolean checkParam(CmdSocketHandler handler, String[] params) {
    return true;
  }

  public boolean checkConfirm() {
    return false;
  }

  public abstract String getHelp();

  public String getName() {
    return name;
  }
}
