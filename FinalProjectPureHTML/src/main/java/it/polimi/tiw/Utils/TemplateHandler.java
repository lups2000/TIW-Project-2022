package it.polimi.tiw.Utils;

import javax.servlet.ServletContext;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

//class to setup the Thymeleaf template engine
public class TemplateHandler {
	
	public static TemplateEngine getEngine(ServletContext context, String suffix) {
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(context);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		TemplateEngine templateEngine = new TemplateEngine();
		templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(suffix);
		
		return templateEngine;
	}
	

}
