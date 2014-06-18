package com.hp.autonomy.frontend.authentication;

import javax.servlet.http.HttpSession;

import com.autonomy.user.admin.dto.UserRoles;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping({"/user/user", "/config/user"})
public class UserController {

	@RequestMapping("/current-user")
	@ResponseBody
	public UserRoles getCurrentUser(final HttpSession session) {
        // in config mode, this will return 'null', which is deliberate, since we don't want to prevent the built-in
        // admin user from creating/deleting a user who just happens to be called 'admin' in community.
		return (UserRoles) session.getAttribute("user");
	}

}
