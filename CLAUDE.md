# CLAUDE.md

## プロジェクト概要

**plate** は、バッチやサーバープロセスの起動・停止・管理を行うJavaミドルウェア。
Telnet経由のコマンドインターフェースで、サービスやバッチジョブのライフサイクルを制御する。

- グループID: `com.uchicom`
- ライセンス: Apache License 2.0
- Java バージョン: 25
- ビルドツール: Maven

## ビルド・テストコマンド

コード変更後の基本手順（フォーマット → テスト）:

```bash
mvn spotless:apply verify
```

クリーンビルドが必要な場合:

```bash
mvn spotless:apply clean compile verify
```

その他:

```bash
# フォーマットのみ
mvn spotless:apply

# 全テスト
mvn verify

# 特定クラスのテスト
mvn test "-Dtest=com.uchicom.plate.MainTest"

# サーバ起動
mvn exec:java "-Dexec.mainClass=com.uchicom.plate.Main" "-Dexec.args=-file src/test/resources/config.yml"
```

## コーディング規約

- **フォーマッター**: Spotless + Google Java Format 1.28.0 — コード変更後は必ず `mvn spotless:apply` を実行する
- **静的解析**: Error Prone（コンパイル時に自動チェック）
- **ライセンスヘッダー**: Spotlessが自動付与するため手動で書かない（`// (C) $YEAR uchicom`）
- **未使用インポート**: Spotlessが自動削除するため手動で整理しない
- `mvn verify` はフォーマットチェックを含むため、先に `mvn spotless:apply` を実行しないと失敗する

## パッケージ構成

| パッケージ | 説明 |
|-----------|------|
| `com.uchicom.plate` | コアクラス（`Main`, `KeyInfo`, `Porter`, `Starter`, `Commander`） |
| `com.uchicom.plate.cmd` | Telnetコマンド実装（`key/`, `port/`, `util/`, `deploy/`, `github/`） |
| `com.uchicom.plate.dto` | 設定ファイル（YAML）のDTOクラス |
| `com.uchicom.plate.enumeration` | 列挙型 |
| `com.uchicom.plate.exception` | 例外クラス |
| `com.uchicom.plate.factory.di` | DIファクトリー |
| `com.uchicom.plate.handler` | Telnetハンドラー |
| `com.uchicom.plate.scheduler` | スケジュール管理（Cron） |
| `com.uchicom.plate.service` | サービス層（GitHub連携、デプロイ） |
| `com.uchicom.plate.util` | ユーティリティ |

## テスト方針

- テストフレームワーク: JUnit Jupiter, Mockito, AssertJ
- カバレッジ計測: JaCoCo（`com.uchicom.plate.*` パッケージ対象）
- テストコードは `src/test/java/` 以下に配置する
