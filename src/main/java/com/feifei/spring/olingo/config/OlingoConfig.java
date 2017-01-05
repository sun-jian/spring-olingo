package com.feifei.spring.olingo.config;

import com.feifei.spring.olingo.data.Storage;
import com.feifei.spring.olingo.service.DemoEdmProvider;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.Processor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class OlingoConfig {

    @Bean
    public Storage storage() {
        return new Storage();
    }

    @Bean
    public HandlerAdapter olingoHandlerAdapter() {
        return new HandlerAdapter() {
            @Override
            public boolean supports(Object handler) {
                return handler instanceof ODataHttpHandler;
            }

            @Override
            public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                ((ODataHttpHandler) handler).process(request, response);
                return null;
            }

            @Override
            public long getLastModified(HttpServletRequest request, Object handler) {
                return -1;
            }
        };
    }

    @Bean
    public HandlerMapping olingoHandlerMapping(List<Processor> processors) {
        return request -> {
            HttpSession session = request.getSession(true);
            Storage storage = (Storage) session.getAttribute(Storage.class.getName());
            if (storage == null) {
                storage = new Storage();
                session.setAttribute(Storage.class.getName(), storage);
            }
            OData odata = OData.newInstance();
            ServiceMetadata edm = odata.createServiceMetadata(new DemoEdmProvider(), new ArrayList<>());
            ODataHttpHandler handler = odata.createHandler(edm);
            for (Processor processor : processors) {
                handler.register(processor);
            }

            return new HandlerExecutionChain(handler);
        };
    }
}
