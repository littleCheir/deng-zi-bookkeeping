package com.example.expensetracker.user;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 应用启动后初始化所有者账号（仅创建，不重置密码）。
 */
@Component
public class InitialOwnerInitializer implements ApplicationRunner {

    private final UserService userService;

    public InitialOwnerInitializer(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(ApplicationArguments args) {
        userService.ensureInitialOwner();
    }
}
