package liqp.filters;

import liqp.Template;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class LastTest {

    @Test
    public void applyTest() throws RecognitionException {


        Template template = Template.parse("{{numbers | last}}");

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("numbers", new String[]{ "Mu", "foo", "bar" });

        String rendered = String.valueOf(template.render(variables));

        assertThat(rendered, is("bar"));
    }
}