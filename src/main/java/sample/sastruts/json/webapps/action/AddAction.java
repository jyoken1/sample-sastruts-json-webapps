package sample.sastruts.json.webapps.action;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.seasar.struts.annotation.ActionForm;
import org.seasar.struts.annotation.Execute;
import org.seasar.struts.util.ResponseUtil;
import sample.sastruts.json.webapps.dto.AddDto;
import sample.sastruts.json.webapps.form.AddForm;

import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.Set;

public class AddAction {

    @ActionForm
    @Resource
    protected AddForm addForm;

    @Execute(validator = false)
    public String submit() throws JsonProcessingException {
        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        Validator v = vf.getValidator();
        Set<ConstraintViolation<AddForm>> validateResult = v.validate(addForm);

        AddDto result = new AddDto();
        if (!validateResult.isEmpty()) {
            result.messages = new ArrayList<>();
            for (ConstraintViolation<AddForm> cv : validateResult) {
                result.messages.add(cv.getMessage());
            }
        } else {
            result.result = Integer.valueOf(addForm.arg1) + Integer.valueOf(addForm.arg2);
        }

        ObjectMapper mapper = new ObjectMapper();
        ResponseUtil.write(mapper.writeValueAsString(result), "application/json", "UTF-8");

        return null;
    }

}
