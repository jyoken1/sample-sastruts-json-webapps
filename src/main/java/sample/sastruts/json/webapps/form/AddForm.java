package sample.sastruts.json.webapps.form;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Digits;

public class AddForm {

    @NotEmpty
    @Digits(integer = 9, fraction = 0)
    public String arg1;

    @NotEmpty
    @Digits(integer = 9, fraction = 0)
    public String arg2;
}
