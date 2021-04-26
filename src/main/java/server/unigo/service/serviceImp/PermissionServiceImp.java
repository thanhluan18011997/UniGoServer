package server.unigo.service.serviceImp;

import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import server.unigo.dto.PermissionsDTO;
import server.unigo.map.PermissionMapper;
import server.unigo.model.Permissions;
import server.unigo.model.Roles;
import server.unigo.model.Users;
import server.unigo.repository.PermissionRepository;
import server.unigo.repository.RoleRepository;
import server.unigo.repository.UserRepository;
import server.unigo.service.PermissionService;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImp implements PermissionService {
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public PermissionServiceImp(PermissionRepository permissionRepository, UserRepository userRepository, RoleRepository roleRepository) {
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public void modifyPermission(Set<String > permissionsDTONameSet, String id) {
        PermissionMapper permissionMapper = Mappers.getMapper(PermissionMapper.class);
        Optional<Users> usersOptional = userRepository.findByUsername(id);
        if (usersOptional.isPresent()){
            Users user=usersOptional.get();
            Set<Permissions> permissionsSet = permissionsDTONameSet.stream().map(t -> permissionRepository.findByPermissionName(t).get()).collect(Collectors.toSet());
            Roles roles=new Roles("ROLE_modified", "Role of "+id);
            roles.setPermissions(permissionsSet);
            roles.setUsers(Arrays.asList(user).stream().collect(Collectors.toSet()));
            roleRepository.save(roles);
            user.setRoles(Arrays.asList(roles).stream().collect(Collectors.toSet()));
            userRepository.save(user);

        }
        else new ResponseStatusException(HttpStatus.NOT_FOUND, "Nott found " + id);

    }

    @Override
    public void deletePermission(String permissionName) {
        Optional<Permissions> permission = permissionRepository.findByPermissionName(permissionName);
        if (permission.isPresent())
            permissionRepository.delete(permission.get());
        else new ResponseStatusException(HttpStatus.NOT_FOUND, "Nott found " + permissionName);
    }


    @Override
    public Set<PermissionsDTO> getPermissionByID(String id) {
        PermissionMapper permissionMapper = Mappers.getMapper(PermissionMapper.class);
        Optional<Users> usersOptional = userRepository.findByUsername(id);
        if (usersOptional.isPresent())
        {
            return usersOptional.get().getRoles().stream().findFirst().get().getPermissions()
                    .stream().map(t->permissionMapper.mapEntityToDTo(t)).collect(Collectors.toSet());
        }
        else new ResponseStatusException(HttpStatus.NOT_FOUND, "Nott found Id=" + id);
        return null;
    }
}