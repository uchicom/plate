# AGENTS.md

このファイルはAIエージェント（Claude Codeなど）向けのガイドラインです。

## プロジェクト概要

**plate** は、バッチやサーバープロセスの起動・停止・管理を行うJavaミドルウェアです。
Telnet経由のコマンドインターフェースで、サービスやバッチジョブのライフサイクルを制御します。

- グループID: `com.uchicom`
- ライセンス: Apache License 2.0
- Java バージョン: 25
- ビルドツール: Maven

## ビルド・テスト手順

コードを変更したら、以下の順番でコマンドを実行してください。

### フォーマット + 全テスト（基本）

```
mvn spotless:apply verify
```

### フォーマット + クリーンビルド + 全テスト（確実に通したい場合）

```
mvn spotless:apply clean compile verify
```

### フォーマットのみ

```
mvn spotless:apply
```

### 全テスト実行のみ

```
mvn verify
```

### 特定クラスのテスト実行

```
mvn test "-Dtest=com.uchicom.plate.MainTest"
```

### サーバ起動（動作確認）

```
mvn exec:java "-Dexec.mainClass=com.uchicom.plate.Main" "-Dexec.args=-file src/test/resources/config.yml"
```

## コーディング規約

- **フォーマッター**: Spotless + Google Java Format 1.28.0
- コードを変更したら必ず `mvn spotless:apply` を実行すること
- フォーマットが合っていないと `mvn verify` が失敗する
- **静的解析**: Error Prone（コンパイル時に自動チェック）
- **ライセンスヘッダー**: 各Javaファイルの先頭に `// (C) $YEAR uchicom` が必要
- Spotlessが自動付与するため、手動で追加しない
- **インポート**: 未使用インポートはSpotlessが自動削除する

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

## 注意事項

- `mvn verify` はフォーマットチェックも行うため、コード変更後は必ず `mvn spotless:apply` を先に実行すること
- コマンド引数にスペースが含まれる場合は引用符で囲む（`"-Dexec.args=..."`）
