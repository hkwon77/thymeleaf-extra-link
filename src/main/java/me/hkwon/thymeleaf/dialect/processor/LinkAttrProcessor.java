package me.hkwon.thymeleaf.dialect.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.springframework.web.servlet.support.RequestContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.engine.EngineEventUtils;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.spring4.naming.SpringContextVariableNames;
import org.thymeleaf.standard.expression.Assignation;
import org.thymeleaf.standard.expression.AssignationSequence;
import org.thymeleaf.standard.expression.LinkExpression;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.util.StringUtils;
import org.unbescape.html.HtmlEscape;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hkwon
 */
@Slf4j
public class LinkAttrProcessor extends AbstractAttributeTagProcessor {
    public static final int ATTR_PRECEDENCE = 1300;
    public static final String ATTR_NAME = "link";
    private static final char PARAMS_START_CHAR = '(';
    private static final char PARAMS_END_CHAR = ')';
    private static final char EXPRESSION_END_CHAR = '}';
    private static final char PARAMS_DELIMITER = ',';
    private String charset;

    public LinkAttrProcessor(TemplateMode templateMode, String dialectPrefix, String charset) {
        super(templateMode, dialectPrefix,  null, false, ATTR_NAME, true, ATTR_PRECEDENCE, true);
        this.charset = charset;
    }

    @Override
    protected void doProcess(ITemplateContext context,
                             IProcessableElementTag tag, AttributeName attributeName,
                             String attributeValue, IElementTagStructureHandler structureHandler) {
        final RequestContext requestContext = (RequestContext)context.getVariable(SpringContextVariableNames.SPRING_REQUEST_CONTEXT);
        final LinkExpression linkExpression;
        final Object expressionResult;

        if (StringUtils.isEmptyOrWhitespace(attributeValue)) {
            expressionResult = null;
        } else {
            // Get Attribute expression
            linkExpression = (LinkExpression) StandardExpressions.getExpressionParser(context.getConfiguration()).parseExpression(context, attributeValue);

            if (linkExpression == null) {
                expressionResult = null;
            } else {
                if (requestContext.getQueryString() == null) {
                    expressionResult = linkExpression.execute(context);
                } else {
                    // Append whole request parameters to attributeValue
                    URI uri = null;
                    List<NameValuePair> nvp = null;

                    try {
                        uri = new URI(requestContext.getRequestUri() + "?" + requestContext.getQueryString());
                        nvp = URLEncodedUtils.parse(uri, Charset.forName(charset));
                    } catch (URISyntaxException e) {
                        log.error("Passed URI has not valid syntax : " + uri, e);
                    }

                    // Exclude duplication query string
                    AssignationSequence assignationSequence = linkExpression.getParameters();

                    if (assignationSequence != null) {
                        for (Assignation assignation : assignationSequence) {
                            nvp.removeIf(e -> assignation.getLeft().getStringRepresentation().equals(e.getName()));
                        }
                    }

                    final String parameters = nvp.stream()
                            .map(nv -> nv.getName() + "=${'" + nv.getValue() + "'}")
                            .collect(Collectors.joining(","));

                    final StringBuilder sb = new StringBuilder();

                    if (linkExpression.hasParameters()) {
                        // Manipulate expression string with request parameters
                        final int lastIndex = attributeValue.lastIndexOf(PARAMS_END_CHAR);

                        sb.append(attributeValue.substring(0, lastIndex))
                                .append(PARAMS_DELIMITER)
                                .append(parameters)
                                .append(attributeValue.substring(lastIndex, attributeValue.length()));

                    } else {
                        sb.append(attributeValue.substring(0, attributeValue.lastIndexOf(EXPRESSION_END_CHAR)))
                                .append(PARAMS_START_CHAR)
                                .append(parameters)
                                .append(PARAMS_END_CHAR)
                                .append(EXPRESSION_END_CHAR);
                    }

                    attributeValue = sb.toString();

                    expressionResult = EngineEventUtils.computeAttributeExpression(context, tag, attributeName, attributeValue).execute(context);
                }
            }
        }

        structureHandler.setAttribute("href", HtmlEscape.escapeHtml4Xml(expressionResult == null ? null : expressionResult.toString()));
    }
}