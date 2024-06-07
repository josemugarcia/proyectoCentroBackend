package com.hospital.serviceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.hospital.JWT.CustomerUserDetailsService;
import com.hospital.JWT.JwtFilter;
import com.hospital.JWT.JwtUtil;
import com.hospital.POJO.User;
import com.hospital.constents.HospitalConstant;
import com.hospital.service.UserService;
import com.hospital.utils.HospitalUtils;
import com.hospital.wrapper.UserWrapper;
import com.hospital.dao.UserDao;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserDao userDao;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    CustomerUserDetailsService customerUserDetailsService;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired

    JwtFilter jwtFilter;

    @Autowired
    EmailService emailService;

    @Override
    public ResponseEntity<String> signUp(Map<String, String> requestMap) {
        log.info("inside signup {}", requestMap);
        try {
            if (validateSignUpMap(requestMap)) {
                User user = userDao.findByEmailId(requestMap.get("email"));
                String nombreUsuario = requestMap.get("nombre");
                if (Objects.isNull(user)) {
                    userDao.save(getUserFromMap(requestMap));

                    // // Envía un correo de confirmación al nuevo usuario
                    String emailEnvio = requestMap.get("email");
                    String cuerpo = "Gracias por registrarte en nuestro sistema. Tu cuenta ha sido creada exitosamente.";
                    String asunto = "Bienvenido, " + nombreUsuario + "!";
                    emailService.sendEmail(emailEnvio, cuerpo, asunto);

                    return HospitalUtils.getResponseEntity("Successfully Registered. ", HttpStatus.OK);
                } else {
                    return HospitalUtils.getResponseEntity("El email ya existe", HttpStatus.BAD_REQUEST);
                }
            } else {
                return HospitalUtils.getResponseEntity(HospitalConstant.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return HospitalUtils.getResponseEntity(HospitalConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private boolean validateSignUpMap(Map<String, String> requestMap) {
        if (requestMap.containsKey("nombre") && requestMap.containsKey("telefono")
                && requestMap.containsKey("email") && requestMap.containsKey("password")) {

            return true;
        }
        return false;
    }

    private User getUserFromMap(Map<String, String> requestMap) {
        User user = new User();
        user.setNombre(requestMap.get("nombre"));
        user.setTelefono(requestMap.get("telefono"));
        user.setEmail(requestMap.get("email"));
        user.setPassword(requestMap.get("password"));
        user.setEstado("true");
        user.setRol("user");
        return user;
    }

    private User getUser2FromMap(Map<String, String> requestMap, User existingUser, boolean isAdd) {
        User user = new User();
        if (isAdd) {
            user.setId(Integer.parseInt(requestMap.get("id")));
        }

        user.setNombre(requestMap.get("nombre"));
        user.setTelefono(requestMap.get("telefono"));
        user.setEmail(requestMap.get("email"));

        // Mantener los valores existentes de estado, password y rol
        user.setEstado(existingUser.getEstado());
        user.setPassword(existingUser.getPassword());
        user.setRol(existingUser.getRol());

        // Cargar las citas asociadas al usuario
        existingUser.getCitas().size(); // Esto carga las citas asociadas al usuario

        // Copiar las citas del usuario existente al nuevo usuario
        user.setCitas(existingUser.getCitas());

        return user;
    }

    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap) {
        log.info("Inside login");
        try {
            org.springframework.security.core.Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestMap.get("email"), requestMap.get("password")));
            if (auth.isAuthenticated()) {
                if (customerUserDetailsService.getUserDetails().getEstado().equalsIgnoreCase("true")) {
                    String token = jwtUtil.generateToken(customerUserDetailsService.getUserDetails().getEmail(),
                            customerUserDetailsService.getUserDetails().getRol());
                    String nombreUsuario = customerUserDetailsService.getUserDetails().getNombre();
                    String rolUsuario = customerUserDetailsService.getUserDetails().getRol();
                    int idUsuario = customerUserDetailsService.getUserDetails().getId();
                    // Construye el objeto JSON de respuesta
                    JSONObject responseJson = new JSONObject();
                    responseJson.put("token", token);
                    responseJson.put("nombreUsuario", nombreUsuario);
                    responseJson.put("rolUsuario", rolUsuario);
                    responseJson.put("idUsuario", idUsuario);
                    // Devuelve la respuesta JSON correctamente formateada
                    return new ResponseEntity<>(responseJson.toString(), HttpStatus.OK);
                } else {
                    return new ResponseEntity<String>("{\"message\":\"" + "Wait for admin approval." + "\"}",
                            HttpStatus.UNAUTHORIZED);
                }
            }
        } catch (Exception ex) {
            log.error("{}", ex);
        }
        return new ResponseEntity<String>("{\"message\":\"" + "Bad Credentials." + "\"}", HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<List<UserWrapper>> getAllUser() {
        try {
            if (jwtFilter.isAdmin()) {
                return new ResponseEntity<>(userDao.getAllUser(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateUser(Map<String, String> requestMap) {
        try {
            if (jwtFilter.isAdmin()) {
                if (validateUserMap(requestMap, true)) {
                    Optional<User> optional = userDao.findById(Integer.parseInt(requestMap.get("id")));

                    if (optional.isPresent()) {
                        User existingUser = optional.get();

                        // Actualizar el usuario solo si se proporciona un correo electrónico válido y
                        // único
                        String newEmail = requestMap.get("email");
                        User emailByName = userDao.findByEmail(newEmail);
                        if (emailByName == null || emailByName.getId().equals(existingUser.getId())) {
                            // Obtener el usuario actualizado desde el mapa y el usuario existente
                            User user = getUser2FromMap(requestMap, existingUser, true);
                            // Actualizar la contraseña si se proporciona en la solicitud
                            String newPassword = requestMap.get("password");
                            if (newPassword != null && !newPassword.isEmpty()) {
                                user.setPassword(newPassword);
                            }
                            // Guardar el usuario actualizado en la base de datos
                            userDao.save(user);
                            return HospitalUtils.getResponseEntity("Usuario actualizado correctamente", HttpStatus.OK);
                        } else {
                            return HospitalUtils.getResponseEntity("El email del usuario ya existe",
                                    HttpStatus.BAD_REQUEST);
                        }
                    } else {
                        return HospitalUtils.getResponseEntity("ID del usuario no existe", HttpStatus.BAD_REQUEST);
                    }
                } else {
                    return HospitalUtils.getResponseEntity(HospitalConstant.INVALID_DATA, HttpStatus.BAD_REQUEST);
                }
            } else {
                return HospitalUtils.getResponseEntity(HospitalConstant.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return HospitalUtils.getResponseEntity(HospitalConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private boolean validateUserMap(Map<String, String> requestMap, boolean validateId) {
        if (requestMap.containsKey("nombre")) {
            if (requestMap.containsKey("id") && validateId) {
                return true;
            } else if (!validateId) {
                return true;
            }
        }
        return false;
    }

    

    @Override
    public ResponseEntity<String> checkToken() {
        return HospitalUtils.getResponseEntity("true", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> changePassword(Map<String, String> requestMap) {
        try {
            User userObj = userDao.findByEmail(jwtFilter.getCurrentUser());
            if (!userObj.equals(null)) {
                if (userObj.getPassword().equals(requestMap.get("oldPassword"))) {
                    userObj.setPassword(requestMap.get("newPassword"));
                    userDao.save(userObj);
                    return HospitalUtils.getResponseEntity("Password Updated Succesfully", HttpStatus.OK);
                }
                return HospitalUtils.getResponseEntity("Incorrect Old Password", HttpStatus.BAD_REQUEST);
            }
            return HospitalUtils.getResponseEntity(HospitalConstant.SOMETHING_WENT_WRONG,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception ex) {

            ex.printStackTrace();
        }
        return HospitalUtils.getResponseEntity(HospitalConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // @Override
    // public ResponseEntity<String> forgotPassword(Map<String, String> requestMap)
    // {
    // try {
    // User user = userDao.findByEmail(requestMap.get("email"));
    // if (!Objects.isNull(user) && !Strings.isNullOrEmpty(user.getEmail())) {
    // emailUtils.forgotMail(user.getEmail(), "Credentials by Hospital",
    // user.getPassword());
    // return HospitalUtils.getResponseEntity("Check your mail for Credentials",
    // HttpStatus.OK);
    // }
    // } catch (Exception ex) {
    // ex.printStackTrace();
    // }
    // return HospitalUtils.getResponseEntity(HospitalConstant.SOMETHING_WENT_WRONG,
    // HttpStatus.INTERNAL_SERVER_ERROR);

    // }

    @Override
    public ResponseEntity<String> updateEstado(Map<String, String> requestMap) {
        try {
            if (jwtFilter.isAdmin()) {
                Optional<User> optional = userDao.findById(Integer.parseInt(requestMap.get("id")));

                if (optional.isPresent()) {
                    // Asegúrate de que el método updateStatus esté correctamente definido en
                    // userDao
                    userDao.updateStatus(requestMap.get("estado"), Integer.parseInt(requestMap.get("id")));
                    return HospitalUtils.getResponseEntity("Estado del usuario actualizado", HttpStatus.OK);
                } else {
                    return HospitalUtils.getResponseEntity("El id del usuario no existe", HttpStatus.OK);
                }
            } else {
                return HospitalUtils.getResponseEntity(HospitalConstant.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return HospitalUtils.getResponseEntity(HospitalConstant.SOMETHING_WENT_WRONG,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
public ResponseEntity<String> deleteUser(Integer id) {
    try {
        if (jwtFilter.isAdmin()) {
            Optional<User> optional = userDao.findById(id);
            if (optional.isPresent()) {
                // Obtener el usuario
                User user = optional.get();
                
                // Recoger el correo electrónico del usuario
                String email = user.getEmail();

                // Eliminar el usuario
                userDao.deleteById(id);

                String cuerpo = "Lamentamos informarle que su cuenta ha sido eliminada de nuestro centro.";
                String asunto = "Cuenta Eliminada";
                emailService.sendEmail(email, cuerpo, asunto);

                // Ahora puedes usar 'email' para enviar un correo electrónico al usuario si lo deseas

                return HospitalUtils.getResponseEntity("Usuario eliminado correctamente", HttpStatus.OK);
            }
            return HospitalUtils.getResponseEntity("El usuario no existe", HttpStatus.OK);
        } else {
            return HospitalUtils.getResponseEntity(HospitalConstant.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
        }
    } catch (Exception ex) {
        ex.printStackTrace();
    }
    return HospitalUtils.getResponseEntity(HospitalConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
}


    @Override
    public ResponseEntity<List<UserWrapper>> getAdmin() {
        try {
            if (jwtFilter.isAdmin()) {
                return new ResponseEntity<>(userDao.getAdmin(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'forgotPassword'");
    }

}
