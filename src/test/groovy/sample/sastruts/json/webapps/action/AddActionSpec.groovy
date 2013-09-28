package sample.sastruts.json.webapps.action

import org.seasar.framework.mock.servlet.MockHttpServletResponse
import org.shiena.seasar.Seasar2Support
import spock.lang.Specification
import spock.lang.Unroll

import javax.servlet.http.HttpServletResponse

@Seasar2Support
class AddActionSpec extends Specification {
    HttpServletResponse response
    AddAction addAction

    @Unroll
    def '#a1 + #a2 = #json'() {
        setup:
        addAction.addForm.with {
            arg1 = a1
            arg2 = a2
        }

        when:
        def result = addAction.submit()
        def mockHttpServletResponse = response as MockHttpServletResponse

        then:
        result == null
        mockHttpServletResponse.contentType == 'application/json; charset=UTF-8'
        new String(mockHttpServletResponse.responseBytes, 'UTF-8') == json

        where:
        a1            | a2
        '1'           | '2'
        '-3'          | '2'
        '99999999999' | '1'
        '1'           | '99999999999'
        '-9999999999' | '1'
        '1'           | '-9999999999'
        'foobar'      | '1'
        '1'           | 'foobar'
        null          | '1'
        '1'           | null

        json << [
            '{"result":3,"messages":null}',
            '{"result":-1,"messages":null}',
            '{"result":null,"messages":["numeric value out of bounds (<9 digits>.<0 digits> expected)"]}',
            '{"result":null,"messages":["numeric value out of bounds (<9 digits>.<0 digits> expected)"]}',
            '{"result":null,"messages":["numeric value out of bounds (<9 digits>.<0 digits> expected)"]}',
            '{"result":null,"messages":["numeric value out of bounds (<9 digits>.<0 digits> expected)"]}',
            '{"result":null,"messages":["numeric value out of bounds (<9 digits>.<0 digits> expected)"]}',
            '{"result":null,"messages":["numeric value out of bounds (<9 digits>.<0 digits> expected)"]}',
            '{"result":null,"messages":["may not be empty"]}',
            '{"result":null,"messages":["may not be empty"]}',
        ]
    }
}
