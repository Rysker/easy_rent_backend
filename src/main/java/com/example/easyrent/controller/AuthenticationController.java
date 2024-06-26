package com.example.easyrent.controller;

import com.example.easyrent.dto.request.SignInRequest;
import com.example.easyrent.dto.request.SignUpRequest;
import com.example.easyrent.dto.response.JwtAuthenticationResponse;
import com.example.easyrent.dto.response.LoginResponseDto;
import com.example.easyrent.dto.response.MessageDto;
import com.example.easyrent.service.AuthenticationService;

import com.example.easyrent.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController
{
    private final AuthenticationService authenticationService;
    private final UserService userService;
    @PostMapping("/signup")
    public ResponseEntity<MessageDto> signup(@RequestBody SignUpRequest request)
    {
        try
        {
            authenticationService.signUp(request);
            return ResponseEntity.ok( new MessageDto("User registered successfully!"));
        }
        catch (DataIntegrityViolationException e)
        {
            if (e.getMessage().contains("constraint_name"))
                return new ResponseEntity<>( new MessageDto("Email or username already exists."), HttpStatus.INTERNAL_SERVER_ERROR);
            else
                return new ResponseEntity<>( new MessageDto("Invalid data"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        catch(IllegalArgumentException e)
        {
            return new ResponseEntity<>(new MessageDto(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        catch(Exception e)
        {
            return new ResponseEntity<>(new MessageDto("UnknownError!"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<LoginResponseDto> signin(@RequestBody SignInRequest request, HttpServletResponse res)
    {
        try
        {
            JwtAuthenticationResponse response = authenticationService.signIn(request);
            Cookie jwtCookie = new Cookie("jwtCookie", response.getToken());
            jwtCookie.setPath("/");
            jwtCookie.setHttpOnly(true);
            jwtCookie.setMaxAge(3600);
            res.addCookie(jwtCookie);
            return ResponseEntity.ok(new LoginResponseDto("Success", userService.getUserFromToken(response.getToken()).getId()));
        }
        catch(Exception e)
        {
            return new ResponseEntity<>(new LoginResponseDto("UnknownError!", null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/logout")
    public void logout(HttpServletResponse response)
    {
        Cookie authCookie = new Cookie("jwtCookie", null);
        authCookie.setPath("/");
        authCookie.setMaxAge(0);
        response.addCookie(authCookie);
    }
}
