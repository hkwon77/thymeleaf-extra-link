package me.hkwon.thymeleaf.dialect;

import me.hkwon.thymeleaf.dialect.processor.LinkAttrProcessor;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author hkwon
 */
public class ExtraLinkDialect extends AbstractProcessorDialect {
    public static final String NAME = "ExtraLink";
    public static final String DEFAULT_PREFIX = "th";
    public static final int PROCESSOR_PRECEDENCE = 800;
    private String charset;

    public ExtraLinkDialect(String charset) {
        super(NAME, DEFAULT_PREFIX, PROCESSOR_PRECEDENCE);
        this.charset = charset;
    }

    @Override
    public Set<IProcessor> getProcessors(String dialectPrefix) {
        final Set<IProcessor> processors = new LinkedHashSet<IProcessor>();

        processors.add(new LinkAttrProcessor(TemplateMode.HTML, dialectPrefix, charset));

        return processors;
    }
}
