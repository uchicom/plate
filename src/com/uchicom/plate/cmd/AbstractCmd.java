/**
 * (c) 2012 uchicom
 */
package com.uchicom.plate.cmd;

import com.uchicom.plate.Commander;
import com.uchicom.plate.handler.CmdSocketHandler;
import com.uchicom.plate.util.Base64;
import com.uchicom.plate.util.Crypt;

/**
 * @author Uchiyama Shigeki
 *
 */
public abstract class AbstractCmd {

	/** コマンドを束ねるコマンダークラス */
	protected Commander broker;
	
	protected String name;
	/**
	 * コマンダーを保持するコンストラクタ。
	 * @param broker
	 */
	public AbstractCmd(String name, Commander broker) {
		this.broker = broker;
		this.name = name;
	}
	
	/**
	 * コマンドのメイン処理
	 * @param handler
	 * @param params
	 * @return コマンドが正常に完了した場合はtrue,それ以外はfalse
	 */
	public abstract boolean execute(CmdSocketHandler handler, String[] params);
	
	/**
	 * 各コマンドで認証チェックをする。
	 * @param handler
	 * @return 認証の結果OKの場合はtrue,それ以外はfalse
	 */
	public boolean checkAuth(CmdSocketHandler handler) {
		return broker.getMain().getUser() == null && handler.getUser() != null && handler.getPass() != null ||
	    (handler.getUser() != null && handler.getUser().equals(broker.getMain().getUser())) &&
		(handler.getPass() != null && Base64.encode(Crypt.encrypt3(handler.getUser(), handler.getPass())).equals(broker.getMain().getCryptPass()));
	}
	
	/**
	 * 各コマンド実行時にパラメータのチェックを行う。
	 * @param handler
	 * @param params
	 * @return パラメータチェックの結果OKの場合はtrue,それ以外はfalse
	 */
	public boolean checkParam(CmdSocketHandler handler, String[] params) {
		return true;
	}
	
	public boolean checkConfirm() {
	    return false;
	}
	
	/**
	 * ヘルプ情報を取得する。
	 * @return
	 */
	public abstract String getHelp();
	
	/**
	 * コマンド名を取得する。
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * コマンド実行結果がOKの場合のメッセージを取得する。
	 * @return
	 */
	public String getOkMessage() {
		return "OK\r\n";
	}
	
	/**
	 * コマンド実行結果がNGの場合のメッセージを取得する。
	 * @return
	 */
	public String getNgMessage() {
		return "NG\r\n";
	}
}
