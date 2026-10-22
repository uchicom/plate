plate
=====

バッチの起動やサーバの起動を制御するJavaミドルウェアです。
Telnetのコマンドインターフェースから、サービスやバッチジョブの起動・停止・管理ができます。

- Java 25 / Maven
- ライセンス: Apache License 2.0

## 特徴

- 同一JVM内で、`main` メソッドを持つクラスをサービス（常駐）またはバッチ（cron指定）として起動・停止する
- Telnetでコマンドを受け付ける（既定は `localhost:8124`）
- 設定をYAMLファイルで保存・読込できる
- 異常終了したサービスの自動復旧（`auto`）
- GitHubのリリースをダウンロードし、指定ディレクトリへ配備（`download` / `deploy`）

## 起動

```
mvn exec:java "-Dexec.mainClass=com.uchicom.plate.Main" "-Dexec.args=-file src/test/resources/config.yml"
```

実行可能JARは `mvn package` で `target/` 配下に作成されます（`jar-with-dependencies`）。

```
java -jar target/plate-0.0.1-SNAPSHOT-jar-with-dependencies.jar -file config.yml
```

### 起動オプション

| オプション | 既定値 | 説明 |
|-----------|--------|------|
| `-host` | `localhost` | コマンドを受け付けるアドレス。指定した場合は、そのアドレスで外部からも接続を受け付ける |
| `-port` | `8124` | コマンドを受け付けるポート |
| `-file` | なし | 起動時に読み込む設定ファイル（YAML） |

> Telnetは平文で、TLSには対応していません。通常は `-host` を指定せず、localhostのみで使用してください。

## 接続

```
telnet localhost 8124
```

接続後、`user` と `pass` で認証してからコマンドを入力します。コマンド一覧は `help` で表示できます。

```
user admin
pass xxxxx
help
```

ユーザーとパスワードは設定ファイルの `user` と `hash` で決まります。`save` または `cuser`・`cpass` で更新すると、`hash` も更新されます。

## 基本的な使い方

多くのコマンドは、先に `use` で対象のポートを選択する必要があります。

```
use 8080
add test com.uchicom.Test start
addcp test ./test.jar
call test param1 param2
stop
save config.yml
```

## コマンド一覧

`[]` は省略可能な引数です。「use必須」のコマンドは、先に `use` を実行しておく必要があります。

### ポート制御

| コマンド | 書式 | 説明 |
|---------|------|------|
| `use` | `use [hostname:]port` | 操作対象のポートを選択する |
| `addportcp` | `addportcp classPath [hostname:]port` | ポートにクラスパスを追加する |
| `removeportcp` | `removeportcp classPath [hostname:]port` | ポートからクラスパスを削除する |
| `stop` | `stop` | 起動中のキーを停止する（use必須） |
| `auto` | `auto key` | キーを自動復旧にする（use必須） |
| `manual` | `manual key` | キーを手動復旧にする（use必須） |

### キー制御（すべてuse必須）

| コマンド | 書式 | 説明 |
|---------|------|------|
| `add` | `add key className [method]` | ポートにキーを追加する（methodの既定は `main`） |
| `remove` | `remove key` | キーを削除する |
| `edit` | `edit key className2` | キーのクラス名を変更する |
| `rename` | `rename key key2` | キー名を変更する |
| `enable` | `enable key` | キーを有効にする |
| `disable` | `disable key` | キーを無効にする |
| `call` | `call key [param1 ..]` | キーを実行する |
| `addcp` | `addcp key classPath` / `addcp key protocol host filepath` | キーにクラスパスを追加する |
| `removecp` | `removecp key classPath` | キーからクラスパスを削除する |

### 管理

| コマンド | 書式 | 説明 |
|---------|------|------|
| `list` | `list` | キーとクラスの情報を一覧表示する |
| `build` | `build [port:..]` | ClassLoaderを再構築する |
| `purge` | `purge` | 終了済みのStarterを破棄する |
| `dset` | `dset name value` | システムプロパティを設定する |
| `save` | `save filename` | 現在の構成をファイルに保存する |
| `load` | `load filename` | 設定ファイルを読み込む |
| `user` / `pass` | `user username` / `pass password` | 認証情報を入力する |
| `cuser` / `cpass` | `cuser newuser` / `cpass newpassword` | ユーザー、パスワードを変更する |
| `help` | `help` | コマンド一覧、バージョン情報を表示する |
| `exit` | `exit` | セッションを終了する |
| `shutdown` | `shutdown` | plateを停止する |

### リリース管理（use必須）

| コマンド | 書式 | 説明 |
|---------|------|------|
| `lsdl` | `lsdl key` | GitHubのダウンロード対象を一覧表示する |
| `download` | `download key tag` | GitHubからリリースをダウンロードする |
| `deploy` | `deploy key tag` | ダウンロードしたファイルを配備する |

## 設定ファイル（YAML）

サンプルは [src/test/resources/config.yml](src/test/resources/config.yml) にあります。

```yaml
user: admin          # 認証ユーザー
hash: xxxxxxxx       # パスワードのハッシュ（save / cpassで更新される）
service:
  services:
    - key: server1                     # 必須
      className: com.uchicom.hoge.Main # 必須
      method:
        startup: main                  # 既定 main
        shutdown: shutdown
      parameters: [a, b, c, d]
      disabled: false                  # 既定 false
      recovery: true                   # 既定 true
      order:
        startup: 1                     # 起動順。既定 0
        shutdown: 1                    # 停止順。既定 0
batch:
  batches:
    - key: batch1                      # 必須
      className: com.uchicom.hoge.Main # 必須
      method:
        startup: main
      parameters: [a, b, c, d]
      disabled: false
      schedule:
        cron: 1 2 3 4 5                # cron形式
release:
  hoge:
    dirPath: release                   # ダウンロード先
    auto:
      detect: ^hoge_(v[0-9]{8}-[0-9]{6}).rel$  # 検知するファイル名
      service: server1                 # 検知したときに対象とするサービス
    github:
      token: <github_token>
      repos: uchicom/plate
      downloadFiles:
        - kind: ASSETS                 # ASSETS / ZIPBALL / TARBALL
          filter: ".*.jar"
    deploy:
      deployFiles:
        - from: "glob:test1.jar"       # 単純移動
          to: hoge/lib/
        - from: "glob:test1.zip"       # アーカイブを展開して配備
          decompress: "glob:**test.htm"
          to: hoge/www/
```

`schedule` の `minute`、`hour`、`day`、`month` による個別指定もサンプルにありますが、`dayOfWeek` は未対応です。

## 開発

```bash
# フォーマット + 全テスト
mvn spotless:apply verify

# フォーマットのみ
mvn spotless:apply

# 特定クラスのテスト
mvn test "-Dtest=com.uchicom.plate.MainTest"
```

`mvn verify` はフォーマットチェックを含むため、先に `mvn spotless:apply` を実行してください。

## パッケージ構成

| パッケージ | 説明 |
|-----------|------|
| `com.uchicom.plate` | コアクラス（`Main`, `KeyInfo`, `Porter`, `Starter`, `Commander`） |
| `com.uchicom.plate.cmd` | Telnetコマンド実装 |
| `com.uchicom.plate.dto` | 設定ファイル（YAML）のDTO |
| `com.uchicom.plate.handler` | Telnetハンドラー |
| `com.uchicom.plate.scheduler` | スケジュール管理（Cron） |
| `com.uchicom.plate.service` | サービス層（GitHub連携、デプロイ） |
