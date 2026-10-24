# test-jakartaee10-profile

Jakarta EE 10（Servlet + Jakarta Faces、Spring なし）で、`APP_ENV` によって CSS のライブラリ、Java のプロパティファイル、Log4j2 の設定ファイルが切り替わるかを確かめるサンプルです。MicroProfile Config は `mp.config.profile` で切り替わります。ログレベルは、選ばれた Log4j2 設定ファイルの既定値を `LOG_LEVEL` で上書きできます。

- JDK 21 / Maven / Jakarta EE Web Profile 10 / MicroProfile Config 3.1 / SLF4J 2 + Log4j 2.25.1
- JBoss EAP 8.1（Jakarta EE 10）向け。web.xml は使いません

## 構成

```
pom.xml
src/main/java/com/example/config/AppEnv.java              # APP_ENV の CDI Bean。CSS のライブラリ名
src/main/java/com/example/config/AppProperties.java       # application-*.properties を Properties で読む
src/main/java/com/example/config/MpConfigValues.java      # @ConfigProperty で MicroProfile Config の値を受ける
src/main/java/com/example/web/HomeServlet.java           # コンテキストルートで画面を表示
src/main/webapp/index.xhtml                              # Faces の画面。library で env.css を選ぶ
src/main/webapp/resources/common.css                     # 全環境共通。CSS 変数を参照する
src/main/webapp/resources/dev/env.css                    # 水色の背景と "DEV"
src/main/webapp/resources/prod/env.css                   # ピンクの背景と "PROD"
src/main/webapp/WEB-INF/beans.xml                        # CDI を有効にする
src/main/webapp/WEB-INF/faces-config.xml                 # FacesServlet を *.xhtml に登録する
src/main/resources/log4j2.component.properties           # APP_ENV に応じた Log4j2 設定を選ぶ
src/main/resources/log4j2-dev.xml                        # 既定は DEBUG。LOG_LEVEL で上書きできる
src/main/resources/log4j2-prod.xml                       # 既定は INFO。LOG_LEVEL で上書きできる
src/main/resources/application-dev.properties            # Java Properties の dev 用
src/main/resources/application-prod.properties           # Java Properties の prod 用
src/main/resources/META-INF/microprofile-config.properties      # MicroProfile Config の共通。未設定時はこれ
src/main/resources/META-INF/microprofile-config-dev.properties  # mp.config.profile=dev のとき。共通より優先
src/main/webapp/WEB-INF/jboss-deployment-structure.xml   # WAR 内の SLF4J + Log4j2 を使う
```

CSS は `h:outputStylesheet` の `library="#{appEnv.value}"` で切り替えます。`env.css` は `resources/dev` または `resources/prod` から、`common.css` は `resources` 直下から配信されます。

## ビルド

```
mvn clean package
```

`target/test-jakartaee10-profile.war` ができます。

## EAP 8.1 へのデプロイ

方法1：WAR をコピーする

```
copy target\test-jakartaee10-profile.war %JBOSS_HOME%\standalone\deployments\
```

方法2：jboss-cli を使う（EAP を起動した状態で）

```
%JBOSS_HOME%\bin\jboss-cli.bat --connect --command="deploy target\test-jakartaee10-profile.war --force"
```

## APP_ENV の指定

指定しないときは `prod` になります。

システムプロパティで指定する場合：

```
standalone.bat -DAPP_ENV=dev
```

環境変数で指定する場合：

```
set APP_ENV=dev
standalone.bat
```

`AppEnv` は MicroProfile Config の `APP_ENV` を読みます。未設定のときの既定値は `prod` です。システムプロパティは環境変数より優先されます。CSS のライブラリ名、プロパティファイル、Log4j2 の設定ファイルはこの値で起動時に1回だけ決まります。画面では `#{appEnv.value}` として参照します。

OpenShift では `oc set env deployment/<eap-app> APP_ENV=dev` で指定できます。

## ログレベル

`log4j2.component.properties` が `log4j2-${APP_ENV}.xml` を読みます。アプリのログは SLF4J から Log4j2 へ渡します。`com.example` と root のレベルは、そのファイルの既定値です。`LOG_LEVEL` を指定すると、どちらのファイルでもそのレベルになります。`LOG_LEVEL` もシステムプロパティ、環境変数の順です。

| APP_ENV | LOG_LEVEL | com.example | root | 画面の DEBUG |
|---|---|---|---|---|
| なし / prod | なし | INFO | INFO | 無効 |
| dev | なし | DEBUG | DEBUG | 有効 |
| なし / prod | DEBUG | DEBUG | DEBUG | 有効 |
| dev | INFO | INFO | INFO | 無効 |

```
set APP_ENV=dev
set LOG_LEVEL=INFO
standalone.bat
```

このときは dev の設定ファイルを読み、レベルは INFO です。システムプロパティで指定する場合は `standalone.bat -DAPP_ENV=dev -DLOG_LEVEL=INFO` です。

`/` を開くと `app-env-log-check` を DEBUG と INFO で出力します。レベルが INFO のときは INFO だけが EAP のログに出ます。EAP 標準の SLF4J と Log4j API を外して WAR 内の Log4j2 を使うため、`jboss-deployment-structure.xml` で `org.slf4j`、`org.slf4j.impl`、`org.apache.logging.log4j.api` を除外しています。どのファイルを読むかとレベルは、起動時に1回だけ決まります。

## プロパティファイル

`AppProperties` が `java.util.Properties` で `application-${APP_ENV}.properties` を1回読みます。未設定のときは `application-prod.properties` です。

同じキーの環境変数があれば、ファイルの値より優先します。キーの `.` を `_` にして大文字にした名前です。

| キー | 環境変数 | dev のファイル | prod のファイル |
|---|---|---|---|
| app.profile.label | APP_PROFILE_LABEL | DEV | PROD |
| app.message | APP_MESSAGE | これは dev のプロパティです | これは prod のプロパティです |

画面の `app.profile.label` と `app.message` で、ファイルの切り替えと環境変数による上書きを確認できます。

## MicroProfile Config

`mp.config.profile` で切り替えます。`APP_ENV` とは別です。未設定のときは `META-INF/microprofile-config.properties` だけを読みます。`dev` のときは、それに加えて `META-INF/microprofile-config-dev.properties` を読み、同じキーはこちらが優先されます。

システムプロパティで指定する場合：

```
standalone.bat -Dmp.config.profile=dev
```

環境変数で指定する場合：

```
set MP_CONFIG_PROFILE=dev
standalone.bat
```

ファイルの値は `${環境変数名:既定値}` で環境変数を参照します。環境変数が無いときは、コロン以降が使われます。

```properties
mp.message=${MP_MESSAGE:これは prod の MicroProfile Config です}
```

`MP_MESSAGE` を指定すると、`mp.message` はその値になります。キー名を `.` から `_` にして大文字にした環境変数は、ファイルより優先されるためです。`mp.profile.label` の環境変数名は `MP_PROFILE_LABEL` です。

| キー | 環境変数 | dev | 未設定（共通ファイル） |
|---|---|---|---|
| mp.profile.label | MP_PROFILE_LABEL | DEV | PROD |
| mp.message | MP_MESSAGE | これは dev の MicroProfile Config です | これは prod の MicroProfile Config です |

```
set MP_CONFIG_PROFILE=dev
set MP_MESSAGE=環境変数で上書き
standalone.bat
```

このとき `mp.profile.label` は `DEV`、`mp.message` は `環境変数で上書き` になります。`MP_MESSAGE` を外して再起動すると、dev の既定文言に戻ります。

## 確認する URL

- http://localhost:8080/test-jakartaee10-profile/
- http://localhost:8080/test-jakartaee10-profile/jakarta.faces.resource/env.css.xhtml?ln=prod
- http://localhost:8080/test-jakartaee10-profile/jakarta.faces.resource/env.css.xhtml?ln=dev
- http://localhost:8080/test-jakartaee10-profile/jakarta.faces.resource/common.css.xhtml

| 指定 | 画面 | env.css の URL |
|---|---|---|
| なし | ピンクの背景に "PROD"、APP_ENV は prod | `...?ln=prod` |
| dev | 水色の背景に "DEV"、APP_ENV は dev | `...?ln=dev` |

画面は `library="#{appEnv.value}"` の `env.css` と、ライブラリ無しの `common.css` を読みます。色と表示文言は、`env.css` の CSS 変数（`--env-bg`、`--env-label`、`--env-label-color`）で切り替わります。`common.css` から `@import` は使いません。ライブラリ名は URL の `ln` に入り、相対パスの `@import` はそのクエリを引き継がないためです。

画面には、解決した `APP_ENV` と、システムプロパティ・環境変数の生の値も表示されます。プロパティの確認例は次のとおりです。

```
set APP_ENV=dev
set APP_MESSAGE=環境変数で上書き
standalone.bat
```

このとき `app.profile.label` は `DEV`、`app.message` は `環境変数で上書き` になります。`APP_MESSAGE` を外して再起動すると、dev のファイルに書いたメッセージに戻ります。

## 注意点

- ライブラリ名は起動時に1回だけ決まります。切り替えるときは EAP を再起動してください。
- `resources/{APP_ENV}/env.css` が無い値を指定したときは、`env.css` は 404 になります。`common.css` は `resources` 直下から配信されます。
