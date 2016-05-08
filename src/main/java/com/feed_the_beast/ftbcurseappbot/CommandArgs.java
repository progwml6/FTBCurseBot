package com.feed_the_beast.ftbcurseappbot;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import lombok.Getter;

/**
 * Created by progwml6 on 5/7/16.
 */
public class CommandArgs {
    @Getter
    private static CommandArgs settings;


    static {
        settings = new CommandArgs();
    }
    @Parameter(names = { "--help", "-h" }, help = true, description = "Shows help")
    @Getter
    private boolean help = false;

    @Parameter(names = { "--config", "-c" }, description = "Config file location", arity = 1, validateWith = ValidateRequiredValue.class)
    @Getter
    private String configFile;



    public static class ValidateRequiredValue implements IParameterValidator {
        @Override
        public void validate (String name, String value) throws ParameterException {
            if (value == null || value.isEmpty()) {
                // this should never happen because jcommander bug
                throw new ParameterException("Expected a value after parameter " + name);
            }
            if (value.startsWith("-") && !value.equals("--")) {
                throw new ParameterException("Expected a value after parameter " + name + ". Looks like argument " + value);
            }
        }
    }



}
