package io.protostuff.compiler.cli;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import io.protostuff.compiler.model.ImmutableModuleConfiguration;
import io.protostuff.compiler.model.ModuleConfiguration;
import io.protostuff.compiler.parser.ParserException;
import io.protostuff.generator.CompilerModule;
import io.protostuff.generator.CompilerRegistry;
import io.protostuff.generator.GeneratorException;
import io.protostuff.generator.ProtostuffCompiler;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Kostiantyn Shchepanovskyi
 */
public class ProtostuffCompilerCLI extends ProtostuffCompiler {

    public static final String __VERSION = ProtostuffCompilerCLI.class.getPackage().getImplementationVersion();
    public static final String GENERATOR = "generator";
    public static final String TEMPLATE = "template";
    public static final String JAVA_TEMPLATE = "java_template";
    public static final String EXTENSIONS = "extensions";
    public static final String OUTPUT = "output";
    public static final String DEBUG = "debug";
    public static final String VERSION = "version";
    public static final String HELP = "help";
    public static final String PROTO_PATH = "proto_path";
    public static final String JSON_CONFIG = "json_config";

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtostuffCompilerCLI.class);

    public static void main(String[] args) {
        new ProtostuffCompilerCLI().run(args);
    }

    private static void changeLogLevel(Level newLevel) {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(newLevel);
        ctx.updateLoggers();
    }

    void run(String[] args) {
        CompilerRegistry registry = injector.getInstance(CompilerRegistry.class);
        Options options = new Options();
        options.addOption(Option.builder("h")
                .longOpt(HELP)
                .desc("Print this message.")
                .build());
        options.addOption(Option.builder("I")
                .longOpt(PROTO_PATH)
                .argName("dir")
                .numberOfArgs(1)
                .desc("Specify the directory in which to search for " +
                        "imports.  May be specified multiple times;" +
                        " directories will be searched in order.  If not" +
                        " given, the current working directory is used.")
                .build());
        options.addOption(Option.builder("d")
                .longOpt(DEBUG)
                .desc("Show debug information.")
                .build());
        options.addOption(Option.builder("v")
                .longOpt(VERSION)
                .desc("Show version.")
                .build());
        options.addOption(Option.builder("o")
                .longOpt(OUTPUT)
                .argName("dir")
                .numberOfArgs(1)
                .desc("Specify a directory for saving generated files.")
                .build());
        options.addOption(Option.builder("g")
                .longOpt(GENERATOR)
                .argName("name")
                .numberOfArgs(1)
                .desc("Specify compiler: " + String.join("|", registry.availableCompilers()))
                .build());
        options.addOption(Option.builder("t")
                .longOpt(TEMPLATE)
                .argName("file")
                .numberOfArgs(1)
                .desc("[st4] Specify a template for st4 compiler")
                .build());
        options.addOption(Option.builder("e")
                .longOpt(EXTENSIONS)
                .argName("class")
                .numberOfArgs(1)
                .desc("[st4] Specify full class name of an extensions provider for st4 compiler")
                .build());
        options.addOption(Option.builder()
                .longOpt(JAVA_TEMPLATE)
                .argName("java_template")
                .numberOfArgs(1)
                .desc("[java] Specify a template for java compiler")
                .build());
        options.addOption(Option.builder()
                .longOpt(JSON_CONFIG)
                .argName("json_config")
                .numberOfArgs(1)
                .desc("other config by json string format")
                .build());
        CommandLineParser parser = new DefaultParser();
        ImmutableModuleConfiguration.Builder builder = ImmutableModuleConfiguration.builder();
        builder.name("main");
        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption(HELP)) {
                printHelp(options);
                return;
            }
            if (cmd.hasOption(VERSION)) {
                Package aPackage = ProtostuffCompilerCLI.class.getPackage();
                String version = aPackage.getImplementationVersion();
                System.out.println(version);
                return;
            }
            if (cmd.hasOption(DEBUG)) {
                changeLogLevel(Level.DEBUG);
            }
            if (cmd.hasOption(GENERATOR)) {
                String generator = cmd.getOptionValue(GENERATOR);
                builder.generator(generator);
            } else {
                LOGGER.error("Generator is not set.");
                return;
            }
            if (cmd.hasOption(OUTPUT)) {
                String output = cmd.getOptionValue(OUTPUT);
                builder.output(output);
            } else {
                LOGGER.error("Output directory is not set.");
                return;
            }
            if (cmd.hasOption(TEMPLATE)) {
                List<String> templates = Collections.singletonList(cmd.getOptionValue(TEMPLATE));
                builder.putOptions(CompilerModule.TEMPLATES_OPTION, templates);
            }
            if (cmd.hasOption(EXTENSIONS)) {
                builder.putOptions(CompilerModule.EXTENSIONS_OPTION, cmd.getOptionValue(EXTENSIONS));
            }
            if(cmd.hasOption(JAVA_TEMPLATE)){
                CompilerModule.JAVA_COMPILER_TEMPLATE = cmd.getOptionValue(JAVA_TEMPLATE);
            }
            if(cmd.hasOption(JSON_CONFIG)){
                String jsonString = cmd.getOptionValue(JSON_CONFIG);
                JSONObject jsonObject = JSON.parseObject(jsonString);
                for (String key : jsonObject.keySet()) {
                    String value = jsonObject.getString(key);
                    builder.putOptions(key,value);
                }
            }
            List<Path> includePaths = new ArrayList<>();
            if (cmd.hasOption(PROTO_PATH)) {
                String[] paths = cmd.getOptionValues(PROTO_PATH);
                for (String path : paths) {
                    includePaths.add(Paths.get(path));
                }
            }
            if (includePaths.isEmpty()) {
                includePaths.add(Paths.get("."));
            }
            builder.includePaths(includePaths);
            List<String> protoFiles = cmd.getArgList();
            builder.protoFiles(protoFiles);
        } catch (ParseException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error("Could not parse command", e);
            } else {
                LOGGER.error(e.getMessage());
            }
            return;
        }

        LOGGER.info("Version={}", __VERSION);

        ModuleConfiguration configuration = builder.build();

        if (configuration.getProtoFiles().isEmpty()) {
            LOGGER.error("Missing input file.");
            return;
        }
        if (configuration.getGenerator() == null) {
            LOGGER.error("Missing generator directives.");
            return;
        }
        try {
            compile(configuration);
        } catch (GeneratorException | ParserException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error("Compilation error", e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
    }

    private void printHelp(Options options) {
        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(79);
        Map<String, Integer> order = ImmutableMap.<String, Integer>builder()
                .put(HELP, 1)
                .put(PROTO_PATH, 2)
                .put(GENERATOR, 3)
                .put(OUTPUT, 4)
                .put(TEMPLATE, 5)
                .put(EXTENSIONS, 6)
                .put(DEBUG, 100)
                .build();
        formatter.setOptionComparator((o1, o2) -> Integer.compare(
                order.getOrDefault(o1.getLongOpt(), 99),
                order.getOrDefault(o2.getLongOpt(), 99)));
        formatter.printHelp("protostuff-compiler [options] proto_files", options);
    }

}
