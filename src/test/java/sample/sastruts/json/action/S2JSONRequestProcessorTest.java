package sample.sastruts.json.action;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Enclosed;
import org.junit.runners.Parameterized.Parameters;
import org.seasar.framework.mock.servlet.MockHttpServletRequest;
import org.seasar.framework.unit.Seasar2;
import org.seasar.framework.unit.TestContext;
import org.seasar.framework.util.tiger.ReflectionUtil;
import org.seasar.struts.action.ActionFormWrapperClass;
import org.seasar.struts.config.S2ActionMapping;
import org.seasar.struts.config.S2ExecuteConfig;
import org.seasar.struts.config.S2FormBeanConfig;
import org.seasar.struts.config.S2ModuleConfig;
import org.seasar.struts.util.S2ActionMappingUtil;
import sample.sastruts.json.util.MockHttpServletRequestImpl;
import sample.sastruts.json.webapps.action.AddAction;
import sample.sastruts.json.webapps.form.AddForm;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(Enclosed.class)
public class S2JSONRequestProcessorTest {

    private static Method processMapping;
    private static Method processActionForm;

    @BeforeClass
    public static void beforeClass() {
        processMapping = ReflectionUtil.getDeclaredMethod(
                S2JSONRequestProcessor.class.getSuperclass(), "processMapping",
                HttpServletRequest.class, HttpServletResponse.class, String.class);
        processMapping.setAccessible(true);
        processActionForm = ReflectionUtil.getDeclaredMethod(
                S2JSONRequestProcessor.class.getSuperclass(), "processActionForm",
                HttpServletRequest.class, HttpServletResponse.class, ActionMapping.class);
        processActionForm.setAccessible(true);
    }

    @Ignore
    public static abstract class AbstractTest {
        protected TestContext ctx;
        protected HttpServletRequest request;
        protected HttpServletResponse response;
        protected S2JSONRequestProcessor processor;

        public void before() throws NoSuchMethodException, ServletException {
            ctx.register(AddAction.class, "addAction");
            ctx.register(AddForm.class, "addForm");

            S2ActionMapping mapping = new S2ActionMapping();
            mapping.setPath("/add");
            mapping.setName("addActionForm");
            mapping.setComponentDef(ctx.getComponentDef("addForm"));

            S2ExecuteConfig executeConfig = new S2ExecuteConfig();
            executeConfig.setMethod(getClass().getMethod("getClass"));
            mapping.addExecuteConfig(executeConfig);

            S2ModuleConfig moduleConfig = new S2ModuleConfig("");
            moduleConfig.addActionConfig(mapping);

            ActionFormWrapperClass wrapperClass = new ActionFormWrapperClass(mapping);
            S2FormBeanConfig formConfig = new S2FormBeanConfig();
            formConfig.setName("addActionForm");
            formConfig.setDynaClass(wrapperClass);
            moduleConfig.addFormBeanConfig(formConfig);

            processor = new S2JSONRequestProcessor();
            processor.init(new ActionServlet(), moduleConfig);
        }
    }

    @RunWith(Seasar2.class)
    public static class ConversionToActionFormFromJSON extends AbstractTest {

        private String contentType;
        private String method;
        private String postData;
        private String arg1;
        private String arg2;

        public ConversionToActionFormFromJSON(String contentType, String method, String postData, String arg1, String arg2) {
            this.contentType = contentType;
            this.method = method;
            this.postData = postData;
            this.arg1 = arg1;
            this.arg2 = arg2;
        }

        @Parameters
        public static List<String[]> testData() {
            String[][] datas = new String[][] {
                    {"application/json", "POST", "{\"arg1\":\"3\",\"arg2\":\"5\"}", "3", "5"},
                    {"application/json", "POST", "{}", null, null}
            };
            return Arrays.asList(datas);
        }

        public void test() throws ServletException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {

            MockHttpServletRequestImpl mockHttpServletRequest = new MockHttpServletRequestImpl((MockHttpServletRequest) request);
            mockHttpServletRequest.setContentType(contentType);
            mockHttpServletRequest.setMethod(method);
            mockHttpServletRequest.setReader(new StringReader(postData));

            ActionMapping am = (ActionMapping) processMapping.invoke(processor, mockHttpServletRequest, response, "/add");
            ActionForm actionForm = (ActionForm) processActionForm.invoke(processor, mockHttpServletRequest, response, am);
            processor.processPopulate(mockHttpServletRequest, response, actionForm, am);
            Object resultOfActionForm = S2ActionMappingUtil.getActionMapping().getActionForm();

            assertThat(resultOfActionForm, notNullValue());
            assertThat(resultOfActionForm, instanceOf(AddForm.class));
            assertThat(((AddForm) resultOfActionForm).arg1, is(arg1));
            assertThat(((AddForm) resultOfActionForm).arg2, is(arg2));
        }
    }

    @RunWith(Seasar2.class)
    public static class DoNotConvert extends AbstractTest {

        private String contentType;
        private String method;
        private String postData;

        public DoNotConvert(String contentType, String method, String postData) {
            this.contentType = contentType;
            this.method = method;
            this.postData = postData;
        }

        @Parameters
        public static List<String[]> testData() {
            String[][] datas = new String[][] {
                    {"application/x-www-form-urlencoded", "POST", "{\"arg1\":\"3\",\"arg2\":\"5\"}"},
                    {"application/json", "GET", "{\"arg1\":\"3\",\"arg2\":\"5\"}"},
                    {"application/x-www-form-urlencoded", "GET", "{\"arg1\":\"3\",\"arg2\":\"5\"}"},
            };
            return Arrays.asList(datas);
        }

        public void test() throws ServletException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {

            MockHttpServletRequestImpl mockHttpServletRequest = new MockHttpServletRequestImpl((MockHttpServletRequest) request);
            mockHttpServletRequest.setContentType(contentType);
            mockHttpServletRequest.setMethod(method);
            mockHttpServletRequest.setReader(new StringReader(postData));

            ActionMapping am = (ActionMapping) processMapping.invoke(processor, mockHttpServletRequest, response, "/add");
            ActionForm actionForm = (ActionForm) processActionForm.invoke(processor, mockHttpServletRequest, response, am);
            processor.processPopulate(mockHttpServletRequest, response, actionForm, am);
            Object resultOfActionForm = S2ActionMappingUtil.getActionMapping().getActionForm();

            assertThat(resultOfActionForm, notNullValue());
            assertThat(resultOfActionForm, instanceOf(AddForm.class));
            assertThat(((AddForm) resultOfActionForm).arg1, nullValue());
            assertThat(((AddForm) resultOfActionForm).arg2, nullValue());
        }
    }

    @RunWith(Seasar2.class)
    public static class NullActionForm extends AbstractTest {

        private String contentType;
        private String method;
        private String postData;

        public NullActionForm(String contentType, String method, String postData) {
            this.contentType = contentType;
            this.method = method;
            this.postData = postData;
        }

        @Parameters
        public static List<String[]> testData() {
            String[][] datas = new String[][] {
                    {"application/json", "POST", ""},
                    {"application/json", "POST", "arg1=5&arg2=3"}
            };
            return Arrays.asList(datas);
        }

        public void test() throws ServletException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {

            MockHttpServletRequestImpl mockHttpServletRequest = new MockHttpServletRequestImpl((MockHttpServletRequest) request);
            mockHttpServletRequest.setContentType(contentType);
            mockHttpServletRequest.setMethod(method);
            mockHttpServletRequest.setReader(new StringReader(postData));

            ActionMapping am = (ActionMapping) processMapping.invoke(processor, mockHttpServletRequest, response, "/add");
            processor.processPopulate(mockHttpServletRequest, response, null, am);
            Object resultOfActionForm = S2ActionMappingUtil.getActionMapping().getActionForm();

            assertThat(resultOfActionForm, notNullValue());
            assertThat(resultOfActionForm, instanceOf(AddForm.class));
            assertThat(((AddForm) resultOfActionForm).arg1, nullValue());
            assertThat(((AddForm) resultOfActionForm).arg2, nullValue());
        }
    }

    @RunWith(Seasar2.class)
    public static class InvalidInputParameters extends AbstractTest {

        private String contentType;
        private String method;
        private String postData;

        public InvalidInputParameters(String contentType, String method, String postData) {
            this.contentType = contentType;
            this.method = method;
            this.postData = postData;
        }

        @Parameters
        public static List<String[]> testData() {
            String[][] datas = new String[][] {
                    {"application/json", "POST", ""},
                    {"application/json", "POST", "arg1=5&arg2=3"}
            };
            return Arrays.asList(datas);
        }

        @Test(expected = ServletException.class)
        public void test() throws ServletException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {

            MockHttpServletRequestImpl mockHttpServletRequest = new MockHttpServletRequestImpl((MockHttpServletRequest) request);
            mockHttpServletRequest.setContentType(contentType);
            mockHttpServletRequest.setMethod(method);
            mockHttpServletRequest.setReader(new StringReader(postData));

            ActionMapping am = (ActionMapping) processMapping.invoke(processor, mockHttpServletRequest, response, "/add");
            ActionForm actionForm = (ActionForm) processActionForm.invoke(processor, mockHttpServletRequest, response, am);
            processor.processPopulate(mockHttpServletRequest, response, actionForm, am);
        }
    }
}
