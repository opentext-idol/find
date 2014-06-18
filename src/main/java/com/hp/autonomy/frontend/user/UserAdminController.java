package com.hp.autonomy.frontend.user;

import com.autonomy.login.role.Role;
import com.autonomy.login.role.Roles;
import com.autonomy.user.admin.UserAdmin;
import com.autonomy.user.admin.dto.UserRoles;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping({"/useradmin/config", "/config/config"})
public class UserAdminController {

    @Autowired
    private UserAdmin userAdmin;

    @Autowired
    private Roles roles;

    @RequestMapping(value = "/users", method = {RequestMethod.GET})
    @ResponseBody
    public List<UserRoles> getUsers(
            @RequestParam(value = "role", required = false) final String role,
            @RequestParam(value = "roles", required = false) final List<String> roles,
            @RequestParam(value = "blackList", required = false) final List<String> blackList
    ){

        if(roles != null && blackList != null){
            return this.userAdmin.getUsersRoles(roles, blackList);
        }

        if(roles != null){
            return this.userAdmin.getUsersRoles(roles);
        }

        if(blackList != null){
            return this.userAdmin.getUsersRolesExcept(blackList);
        }

        if(role != null) {
            return this.userAdmin.getUsersRoles(role);
        }

        return this.userAdmin.getUsersRoles();
    }

    @RequestMapping(value = "/users", method = {RequestMethod.POST})
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addUser(@RequestBody final UserRequest userRequest){
        userAdmin.addUser(userRequest.getUsername(), userRequest.getPassword(), userRequest.getRoles().get(0));
    }

    @RequestMapping(value = "/users/{username}", method = {RequestMethod.DELETE})
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable("username") final String name,
                           final HttpSession session){

        if(StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("User must be specified");
        }

        if(name.equalsIgnoreCase((String) session.getAttribute("username"))) {
            throw new IllegalArgumentException("Cannot delete itself");
        }

        userAdmin.deleteUser(name);
    }

    @RequestMapping(value = "/users/{username}", method = {RequestMethod.PATCH})
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void editUser(@RequestBody final UserRequest userRequest, @PathVariable("username") final String name){
        if(StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("User must be specified");
        }

        if (userRequest.getPassword() != null) {
            userAdmin.resetPassword(name, userRequest.getPassword());
        }

        if (userRequest.getRoles() != null){
            final List<String> roles = userAdmin.getUserRole(name);

            for(final Role role : this.roles.getRoles()){
                if(roles.contains(role.getName()) && !userRequest.getRoles().contains(role.getName())){
                    userAdmin.removeUserFromRole(name, role.getName());
                }
            }

            for(final String role : userRequest.getRoles()){
                userAdmin.addUserToRole(name, role);
            }
        }
    }
}