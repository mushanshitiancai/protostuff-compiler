Protocol Buffers parser and code generator
------------------------------------------

[![Build Status](https://travis-ci.org/protostuff/protostuff-compiler.svg?branch=master)](https://travis-ci.org/protostuff/protostuff-compiler)

说明
-----

1. 修改了模板，支持从proto生成POJO，同时如果添加add_tag参数，会添加protostuff的Tag注解和fastjson的JSONField注解。

例子：

```
java -jar protostuff-cli.jar -g java -o ./ Service.proto

java -jar protostuff-cli.jar -g java --add_tag -o ./ Service.proto
```

2. 添加选项enum_to_int,如果设置，则吧proto中的枚举字段编译为int
   
```
java -jar protostuff-cli.jar -g java --enum_to_int -o ./ Service.proto
```

2016年09月28日

1. 添加选项--java_template，支持指定java生成模式的模板，java生成模板的模板可以参考java-template-demo

例子：

```
java_template="/Users/mazhibin/project/template/java/main.stg"
java -jar protostuff-cli.jar -g java_template ${java_template} --java -o ./ Service.proto
```

2. 添加选项--json_config，支持指定一个json字符串作为参数，这些参数会被传入string template4 模板中：

```
java_template="/Users/mazhibin/project/xxx/template/java-bak/main.stg"
other_config='{"java_add_javadoc":true}'
java -jar  protostuff-cli-2.0.0-alpha26-SNAPSHOT-jar-with-dependencies.jar -g java -java_template ${java_template} -o ./ -json_config ${other_config} in.proto%     
```

3. 移除--add_tag和--enum_to_int参数，改为从--json_config传入

Usage
-----

* [maven plugin](https://github.com/protostuff/protostuff-compiler/wiki/Maven-Plugin)
* [command-line interface](https://github.com/protostuff/protostuff-compiler/wiki/Command-line-interface)
 
```xml
    </build>
        <plugins>
            <plugin>
                <artifactId>protostuff-maven-plugin</artifactId>
                <groupId>io.protostuff</groupId>
                <version>2.0.0-alpha16</version>
                <executions>
                    <execution>
                        <id>generate-java-sources</id>
                        <phase>generate-sources</phase>
                        <goals>
                            <goal>java</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```

Java Source Code Generator
--------------------------

Current status: development in progress.

Generated code API: [draft](http://www.protostuff.io/documentation/compiler/java/generated-code).

Documentation Generator
-----------------------

`protostuff-compiler` can generate html from proto files.

Sample output: http://www.protostuff.io/samples/protostuff-compiler/html/#com.example.Address

This generator is an alternative to https://github.com/estan/protoc-gen-doc

Requirements
------------

| Component                                 | Version   |
|-------------------------------------------|-----------|
| JDK                                       | 1.8+      |  
| [Apache Maven](https://maven.apache.org/) | 3.x       |

Build
-----

```
mvn clean install
```
