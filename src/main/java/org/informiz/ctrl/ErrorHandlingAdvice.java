package org.informiz.ctrl;

import com.ibm.cloud.sdk.core.service.exception.InternalServerErrorException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;


// TODO: need to extend a base class?
@ControllerAdvice
public class ErrorHandlingAdvice {
    private static final Logger logger = LoggerFactory.getLogger(ErrorHandlingAdvice.class);

    // TODO: implement handlers for custom exceptions
    @ExceptionHandler({ServletException.class, InternalServerErrorException.class})
    public ModelAndView handleInternalException(ServletException ex, HttpServletRequest request, HttpServletResponse response) {
        logger.error(String.format("Handling error at %s:", request.getRequestURI()), ex);
        ModelAndView mv = new ModelAndView("error");
        mv.addObject("status", response.getStatus());
        mv.addObject("error",  ex.getClass().getSimpleName());
        mv.addObject("message", "An internal error was logged and will be addressed by a developer");

        return mv;
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ModelAndView handleArgumentsException(Exception ex, HttpServletRequest request, HttpServletResponse response) {
        logger.error(String.format("Handling error at %s:", request.getRequestURI()), ex);
        ModelAndView mv = new ModelAndView("error");
        mv.addObject("status", response.getStatus());
        mv.addObject("error",  ex.getClass().getSimpleName());
        mv.addObject("message", "Illegal argument, an error was logged and will be addressed by a developer");

        return mv;
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ModelAndView handleValidationException(ConstraintViolationException ex, HttpServletRequest request, HttpServletResponse response) {
        logger.debug(String.format("Validation error at %s:", request.getRequestURI()), ex);
        ModelAndView mv = new ModelAndView("error");
        mv.addObject("status", response.getStatus());
        mv.addObject("error",  ex.getClass().getSimpleName());
        mv.addObject("message", String.format("Submitted data is invalid: %s", ex.getMessage()));

        return mv;

    }


    @ModelAttribute
    public void addAttributes(Model model, HttpServletRequest request) {
        model.addAttribute("url", request.getServletPath());
    }


}
