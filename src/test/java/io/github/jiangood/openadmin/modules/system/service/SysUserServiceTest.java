package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.framework.config.security.PermissionStaleService;
import io.github.jiangood.openadmin.modules.system.dto.converter.UserConverter;
import io.github.jiangood.openadmin.modules.system.dto.response.UserVO;
import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import io.github.jiangood.openadmin.modules.system.repository.SysMenuRepository;
import io.github.jiangood.openadmin.modules.system.repository.SysRoleRepository;
import io.github.jiangood.openadmin.modules.system.repository.SysUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SysUserServiceTest {

    @Mock
    private SysUserRepository sysUserRepository;
    @Mock
    private SysRoleRepository roleRepository;
    @Mock
    private SysOrgService sysOrgService;
    @Mock
    private SysMenuRepository sysMenuRepository;
    @Mock
    private UserConverter userConverter;
    @Mock
    private PermissionStaleService permissionStaleService;

    private SysUserService sysUserService;
    private PasswordEncoder passwordEncoder;

    @Mock
    private jakarta.persistence.EntityManager entityManager;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        sysUserService = new SysUserService(sysUserRepository, roleRepository, sysOrgService,
                sysMenuRepository, userConverter, permissionStaleService, passwordEncoder);
        ReflectionTestUtils.setField(sysUserService, "repository", sysUserRepository);
        ReflectionTestUtils.setField(sysUserService, "entityManager", entityManager);
    }

    @Test
    void testFindOneDto_whenUserExists() {
        SysUser user = new SysUser();
        user.setId("1");
        user.setAccount("admin");
        user.setName("管理员");

        UserVO expectedVO = new UserVO();
        expectedVO.setId("1");
        expectedVO.setAccount("admin");
        expectedVO.setName("管理员");

        when(sysUserRepository.findById("1")).thenReturn(Optional.of(user));
        when(userConverter.toResponse(user)).thenReturn(expectedVO);

        UserVO result = sysUserService.findOneDto("1");

        assertNotNull(result);
        assertEquals("admin", result.getAccount());
        assertEquals("管理员", result.getName());
        verify(sysUserRepository).findById("1");
    }

    @Test
    void testFindOneDto_whenUserNotExists() {
        when(sysUserRepository.findById("nonexistent")).thenReturn(Optional.empty());

        UserVO result = sysUserService.findOneDto("nonexistent");

        assertNull(result);
    }

    @Test
    void testFindByAccount_whenExists() {
        SysUser user = new SysUser();
        user.setAccount("admin");
        when(sysUserRepository.findByAccount("admin")).thenReturn(Optional.of(user));

        Optional<SysUser> result = sysUserService.findByAccount("admin");

        assertTrue(result.isPresent());
        assertEquals("admin", result.get().getAccount());
    }

    @Test
    void testFindByAccount_whenNotExists() {
        when(sysUserRepository.findByAccount("unknown")).thenReturn(Optional.empty());

        Optional<SysUser> result = sysUserService.findByAccount("unknown");

        assertFalse(result.isPresent());
    }

    @Test
    void testGetNameById_whenUserExists() {
        SysUser user = new SysUser();
        user.setId("1");
        user.setName("管理员");
        when(sysUserRepository.findById("1")).thenReturn(Optional.of(user));

        String name = sysUserService.getNameById("1");

        assertEquals("管理员", name);
    }

    @Test
    void testGetNameById_whenUserNotExists() {
        when(sysUserRepository.findById("nonexistent")).thenReturn(Optional.empty());

        String name = sysUserService.getNameById("nonexistent");

        assertNull(name);
    }

    @Test
    void testGetNameById_withNullId() {
        String name = sysUserService.getNameById(null);

        assertNull(name);
        verify(sysUserRepository, never()).findById(any());
    }

    @Test
    void testDeleteById() {
        SysUser user = new SysUser();
        user.setId("1");
        when(sysUserRepository.findById("1")).thenReturn(Optional.of(user));
        doNothing().when(sysUserRepository).delete(user);

        sysUserService.deleteById("1");

        verify(sysUserRepository).delete(user);
    }

    @Test
    void testUpdatePwd() {
        SysUser user = new SysUser();
        user.setId("1");
        user.setPassword(passwordEncoder.encode("oldPassword"));
        user.setLastPasswordChangeTime(LocalDateTime.now());

        when(sysUserRepository.findById("1")).thenReturn(Optional.of(user));
        when(sysUserRepository.save(any(SysUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        sysUserService.updatePwd("1", "oldPassword", "NewP@ss123");

        verify(sysUserRepository).save(user);
        assertTrue(passwordEncoder.matches("NewP@ss123", user.getPassword()));
        assertNotNull(user.getLastPasswordChangeTime());
    }

    @Test
    void testUpdatePwd_forceChangeSkipsOldPassword() {
        SysUser user = new SysUser();
        user.setId("1");
        user.setPassword(passwordEncoder.encode("adminSetPassword"));

        when(sysUserRepository.findById("1")).thenReturn(Optional.of(user));
        when(sysUserRepository.save(any(SysUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        sysUserService.updatePwd("1", null, "NewP@ss123");

        verify(sysUserRepository).save(user);
        assertTrue(passwordEncoder.matches("NewP@ss123", user.getPassword()));
        assertNotNull(user.getLastPasswordChangeTime());
    }

    @Test
    void testUpdatePwd_withEmptyPassword_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> sysUserService.updatePwd("1", "oldPassword", ""));
    }

    @Test
    void testUpdatePwd_withWrongOldPassword_shouldThrow() {
        SysUser user = new SysUser();
        user.setId("1");
        user.setPassword(passwordEncoder.encode("correctOldPassword"));
        user.setLastPasswordChangeTime(LocalDateTime.now());
        when(sysUserRepository.findById("1")).thenReturn(Optional.of(user));

        assertThrows(IllegalStateException.class, () -> sysUserService.updatePwd("1", "wrongOldPassword", "NewP@ss123"));
    }

    @Test
    void testResetPwd() {
        SysUser user = new SysUser();
        user.setId("1");
        String newPwd = "NewP@ss123";

        when(sysUserRepository.findById("1")).thenReturn(Optional.of(user));
        when(sysUserRepository.save(any(SysUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        sysUserService.resetPwd("1", newPwd);

        verify(sysUserRepository).save(user);
        assertTrue(passwordEncoder.matches(newPwd, user.getPassword()));
        assertNull(user.getLastPasswordChangeTime());
        verify(permissionStaleService).markUserStale(user.getAccount());
    }

    @Test
    void testResetPwd_whenUserNotExists_shouldThrow() {
        when(sysUserRepository.findById("no-such-id")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> sysUserService.resetPwd("no-such-id", "NewP@ss123"));
        verify(sysUserRepository, never()).save(any(SysUser.class));
    }

    @Test
    void testFindValid() {
        SysUser user1 = new SysUser();
        user1.setEnabled(true);
        SysUser user2 = new SysUser();
        user2.setEnabled(true);

        when(sysUserRepository.findAllByEnabledTrue()).thenReturn(java.util.Arrays.asList(user1, user2));

        var result = sysUserService.findValid();

        assertEquals(2, result.size());
        verify(sysUserRepository).findAllByEnabledTrue();
    }

    @Test
    void testGetPermInfo_whenUserNotExists() {
        when(sysUserRepository.findById("no-such-id")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> sysUserService.getPermInfo("no-such-id"));
    }
}