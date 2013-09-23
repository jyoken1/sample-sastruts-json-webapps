package sample.sastruts.json.webapps.action;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Enclosed;
import org.junit.runners.Parameterized.Parameters;
import org.seasar.framework.mock.servlet.MockHttpServletResponse;
import org.seasar.framework.unit.Seasar2;
import sample.sastruts.json.webapps.form.AddForm;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(Enclosed.class)
public class AddActionTest {

    @RunWith(Seasar2.class)
    public static class AddSubTest {

        private HttpServletResponse response;
        private AddAction addAction;

        private String arg1;
        private String arg2;
        private String contentType;
        private String json;

        public AddSubTest(String arg1, String arg2, String contentType, String json) {
            this.arg1 = arg1;
            this.arg2 = arg2;
            this.contentType = contentType;
            this.json = json;
        }

        @Parameters
        public static List<String[]> testData() {
            String[][] datas = new String[][] {
                    {"1", "2", "application/json; charset=UTF-8", "{\"result\":3,\"messages\":null}"},
                    {"-3", "2", "application/json; charset=UTF-8", "{\"result\":-1,\"messages\":null}"}
            };
            return Arrays.asList(datas);
        }

        @Test
        public final void test() throws JsonProcessingException, UnsupportedEncodingException {
            AddForm addForm = new AddForm();
            addForm.arg1 = arg1;
            addForm.arg2 = arg2;
            addAction.addForm = addForm;

            String result = addAction.submit();
            MockHttpServletResponse mockHttpServletResponse = (MockHttpServletResponse) response;

            assertThat(result, nullValue());
            assertThat(mockHttpServletResponse.getContentType(), is(contentType));
            assertThat(new String(mockHttpServletResponse.getResponseBytes(), "UTF-8"), is(json));
        }

    }

    @RunWith(Seasar2.class)
    public static class ErrorTest {

        private HttpServletResponse response;
        private AddAction addAction;

        private String arg1;
        private String arg2;
        private String contentType;
        private String json;

        public ErrorTest(String arg1, String arg2, String contentType, String json) {
            this.arg1 = arg1;
            this.arg2 = arg2;
            this.contentType = contentType;
            this.json = json;
        }

        @Parameters
        public static List<String[]> testData() {
            String[][] datas = new String[][] {
                    {"99999999999", "1", "application/json; charset=UTF-8", "{\"result\":null,\"messages\":[\"numeric value out of bounds (<9 digits>.<0 digits> expected)\"]}"},
                    {"1", "99999999999", "application/json; charset=UTF-8", "{\"result\":null,\"messages\":[\"numeric value out of bounds (<9 digits>.<0 digits> expected)\"]}"},
                    {"-9999999999", "1", "application/json; charset=UTF-8", "{\"result\":null,\"messages\":[\"numeric value out of bounds (<9 digits>.<0 digits> expected)\"]}"},
                    {"1", "-9999999999", "application/json; charset=UTF-8", "{\"result\":null,\"messages\":[\"numeric value out of bounds (<9 digits>.<0 digits> expected)\"]}"},
                    {"foobar", "1", "application/json; charset=UTF-8", "{\"result\":null,\"messages\":[\"numeric value out of bounds (<9 digits>.<0 digits> expected)\"]}"},
                    {"1", "foobar", "application/json; charset=UTF-8", "{\"result\":null,\"messages\":[\"numeric value out of bounds (<9 digits>.<0 digits> expected)\"]}"},
                    {null, "1", "application/json; charset=UTF-8", "{\"result\":null,\"messages\":[\"may not be empty\"]}"},
                    {"1", null, "application/json; charset=UTF-8", "{\"result\":null,\"messages\":[\"may not be empty\"]}"}
            };
            return Arrays.asList(datas);
        }

        @Test
        public final void test() throws JsonProcessingException, UnsupportedEncodingException {
            AddForm addForm = new AddForm();
            addForm.arg1 = arg1;
            addForm.arg2 = arg2;
            addAction.addForm = addForm;

            String result = addAction.submit();
            MockHttpServletResponse mockHttpServletResponse = (MockHttpServletResponse) response;

            assertThat(result, nullValue());
            assertThat(mockHttpServletResponse.getContentType(), is(contentType));
            assertThat(new String(mockHttpServletResponse.getResponseBytes(), "UTF-8"), is(json));
        }
    }
}
