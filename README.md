plate
=====

バッチの起動やサーバの起動を制御します。

## mvn
### サーバ起動
```
mvn exec:java "-Dexec.mainClass=com.uchicom.plate.Main" "-Dexec.args=-file src/test/resources/config.yml"
```

### フォーマッタ
```
mvn spotless:apply
```

### 全体テスト実行
```
mvn verify
```

### ファイル単体でテスト実行
```
mvn test "-Dtest=com.uchicom.plate.MainTest"
```

### フォーマッタ & 全テスト実行
```
mvn spotless:apply verify
```

### フォーマッタ & クリア & 全テスト実行
```
mvn spotless:apply clean compile verify
```
