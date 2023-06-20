package org.informiz.ctrl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import java.io.IOException;

@ControllerAdvice
public class ErrorHandlingAdvice extends DefaultHandlerExceptionResolver {
    private static final Logger logger = LoggerFactory.getLogger(ErrorHandlingAdvice.class);

    // TODO: implement handlers for custom exceptions
    @ExceptionHandler(IOException.class)
    public ModelAndView handleException(IOException ex, HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        logger.error(String.format("Handling error at $s:", request.getRequestURI()), ex);
        ModelAndView mv = new ModelAndView("error");
        mv.addObject("status", response.getStatus());
        mv.addObject("error",  ex.getClass().getSimpleName());
        mv.addObject("message", "An internal error was logged and will be addressed by a developer");

        return mv;

    }
}
