package com.co.crediya.requests.sqs.util;

import java.util.Map;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.StringTemplateResolver;

public class EmailRenderer {

  private final TemplateEngine templateEngine;

  public EmailRenderer() {
    StringTemplateResolver resolver = new StringTemplateResolver();
    resolver.setTemplateMode("HTML");
    resolver.setCacheable(false);

    templateEngine = new TemplateEngine();
    templateEngine.setTemplateResolver(resolver);
  }

  public String render(String templateHtml, Map<String, Object> params) {
    if (params == null) return templateHtml;
    Context context = new Context();
    params.forEach(context::setVariable);
    return templateEngine.process(templateHtml, context);
  }
}
