package com.youdevise.test.scriptwriter;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;

public class Scriptwriter {
    public static void main(String[] args) {
        Configuration config = new Configuration();
        try {
            config.parse(args);
        } catch (ConfigurationException e) {
            System.err.print(config.usage);
            System.err.println(e.getMessage());
            return;
        }

        HTMLPrinter printer = new HTMLPrinter(config.outputDir);
        Interpreter interpreter = new Interpreter(printer);

        String code;
        try {
            code = FileUtils.readFileToString(config.codeFile);
        } catch (IOException e) {
            throw new IllegalStateException("cannot read from file " + config.codeFile.getAbsolutePath());
        }

        interpreter.interpret(code);
        printer.print();
    }

    public static class Interpreter {
        private static Pattern classDeclarationPattern = Pattern.compile("class\\s*(\\w*)");

        private TokenListener listener;
    
        public Interpreter(TokenListener listener) {
            this.listener = listener;
        }
        
        public void interpret(String code) {
            java.util.regex.Matcher classDeclarationMatcher = classDeclarationPattern.matcher(code);
            if (false == classDeclarationMatcher.find()) { 
                throw new IllegalStateException("cannot find class declaration"); 
            }
            String className = classDeclarationMatcher.group(1); 

            listener.giveClassName(className);
        }
    }

    public static interface TokenListener {
        public void giveClassName(String className);
    }

    public static class HTMLPrinter implements TokenListener {
        private File outputDir;
        private String className;

        public HTMLPrinter(File outputDir) { this.outputDir = outputDir; }

        @Override public void giveClassName(String className) { this.className = className; }

        public void print() {
            outputDir.mkdir();
            File outputFile = new File(outputDir, className + ".html");

            try {
                FileUtils.writeStringToFile(outputFile, "<html><head><title>" + className + "</title></head></html>");
            } catch (IOException e) {
                throw new IllegalStateException("cannot write to file " + outputFile.getAbsolutePath());
            }
        }
    }
}
