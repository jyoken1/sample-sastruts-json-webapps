package sample.sastruts.json.action

import org.apache.struts.action.ActionForm
import org.apache.struts.action.ActionMapping
import org.apache.struts.action.ActionServlet
import org.seasar.framework.mock.servlet.MockHttpServletRequest
import org.seasar.framework.unit.TestContext
import org.seasar.struts.action.ActionFormWrapperClass
import org.seasar.struts.config.S2ActionMapping
import org.seasar.struts.config.S2ExecuteConfig
import org.seasar.struts.config.S2FormBeanConfig
import org.seasar.struts.config.S2ModuleConfig
import org.seasar.struts.util.S2ActionMappingUtil
import org.shiena.seasar.Seasar2Support
import sample.sastruts.json.util.MockHttpServletRequestImpl
import sample.sastruts.json.webapps.action.AddAction
import sample.sastruts.json.webapps.form.AddForm
import spock.lang.Specification

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Seasar2Support
class S2JSONRequestProcessorSpec extends Specification {
    TestContext ctx
    HttpServletRequest request
    HttpServletResponse response
    S2JSONRequestProcessor processor

    def setup() {
        ctx.register(AddAction, 'addAction')
        ctx.register(AddForm, 'addForm')
        def executeConfig = new S2ExecuteConfig()
        executeConfig.method = getClass().getMethod('getClass')

        def mapping = new S2ActionMapping()
        mapping.with {
            path = '/add'
            name = 'addActionForm'
            setComponentDef(ctx.getComponentDef('addForm'))
            addExecuteConfig(executeConfig)
        }

        def formConfig = new S2FormBeanConfig()
        formConfig.with {
            name = 'addActionForm'
            dynaClass = new ActionFormWrapperClass(mapping)
        }

        def moduleConfig = new S2ModuleConfig('')
        moduleConfig.with {
            addActionConfig(mapping)
            addFormBeanConfig(formConfig)
        }

        processor = new S2JSONRequestProcessor()
        processor.init(new ActionServlet(), moduleConfig)
    }

    def 'Conversion to ActionForm from JSON'() {
        setup:
        def mockHttpServletRequest = new MockHttpServletRequestImpl(request as MockHttpServletRequest)
        mockHttpServletRequest.with {
            contentType = 'application/json'
            method = 'POST'
            reader = new StringReader(postData)
        }
        def am = processor.processMapping(mockHttpServletRequest, response, '/add') as ActionMapping
        def actionForm = processor.processActionForm(mockHttpServletRequest, response, am) as ActionForm

        when:
        processor.processPopulate(mockHttpServletRequest, response, actionForm, am)
        def resultOfActionForm = S2ActionMappingUtil.actionMapping.actionForm

        then:
        resultOfActionForm != null
        resultOfActionForm instanceof AddForm
        (resultOfActionForm as AddForm).arg1 == arg1
        (resultOfActionForm as AddForm).arg2 == arg2

        where:
        postData                  | arg1 | arg2
        '{"arg1":"3","arg2":"5"}' | '3'  | '5'
        '{}'                      | null | null
    }

    def 'Do not convert'() {
        setup:
        def mockHttpServletRequest = new MockHttpServletRequestImpl(request as MockHttpServletRequest)
        mockHttpServletRequest.with {
            contentType = ct
            method = m
            reader = new StringReader(postData)
        }
        def am = processor.processMapping(mockHttpServletRequest, response, '/add') as ActionMapping
        def actionForm = processor.processActionForm(mockHttpServletRequest, response, am) as ActionForm

        when:
        processor.processPopulate(mockHttpServletRequest, response, actionForm, am)
        def resultOfActionForm = S2ActionMappingUtil.actionMapping.actionForm

        then:
        resultOfActionForm != null
        resultOfActionForm instanceof AddForm
        (resultOfActionForm as AddForm).arg1 == null
        (resultOfActionForm as AddForm).arg2 == null

        where:
        ct                                  | m      | postData
        'application/x-www-form-urlencoded' | 'POST' | '{"arg1":"3","arg2":"5"}'
        'application/json'                  | 'GET'  | '{"arg1":"3","arg2":"5"}'
        'application/x-www-form-urlencoded' | 'GET'  | '{"arg1":"3","arg2":"5"}'
    }

    def 'Null ActionForm'() {
        setup:
        def am = processor.processMapping(request, response, '/add') as ActionMapping

        when:
        processor.processPopulate(request, response, null, am)
        def resultOfActionForm = S2ActionMappingUtil.actionMapping.actionForm

        then:
        resultOfActionForm != null
        resultOfActionForm instanceof AddForm
        (resultOfActionForm as AddForm).arg1 == null
        (resultOfActionForm as AddForm).arg2 == null
    }

    def 'Invalid input parameters'() {
        setup:
        def mockHttpServletRequest = new MockHttpServletRequestImpl(request as MockHttpServletRequest)
        mockHttpServletRequest.with {
            contentType = 'application/json'
            method = 'POST'
            reader = new StringReader(postData)
        }
        def am = processor.processMapping(mockHttpServletRequest, response, '/add') as ActionMapping
        def actionForm = processor.processActionForm(mockHttpServletRequest, response, am) as ActionForm

        when:
        processor.processPopulate(mockHttpServletRequest, response, actionForm, am)

        then:
        thrown(ServletException)

        where:
        postData << ['', 'arg1=5&arg2=3']
    }
}
