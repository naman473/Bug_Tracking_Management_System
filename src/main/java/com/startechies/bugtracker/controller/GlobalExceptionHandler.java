package com.startechies.bugtracker.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Converts uncaught exceptions to user-friendly error pages instead of
 * the default Spring Whitelabel page.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFound(Model model) {
        model.addAttribute("errorTitle",   "Page Not Found");
        model.addAttribute("errorMessage", "The page you requested does not exist.");
        model.addAttribute("errorCode",    "404");
        return "error";
    }

    // Handles missing static resources (e.g. favicon.ico) silently as 404 — not a 500
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String staticResourceNotFound(NoResourceFoundException ex, Model model) {
        model.addAttribute("errorTitle",   "Resource Not Found");
        model.addAttribute("errorMessage", "The requested resource does not exist.");
        model.addAttribute("errorCode",    "404");
        return "error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String serverError(Exception ex, Model model) {
        log.error("Unhandled exception", ex);
        model.addAttribute("errorTitle",   "Something Went Wrong");
        model.addAttribute("errorMessage", "An unexpected error occurred. Please try again.");
        model.addAttribute("errorCode",    "500");
        return "error";
    }
}
