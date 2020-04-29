package swcapstone.freitag.springsecurityjpa;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Transactional
    public boolean signUp(UserDto userDto) {

        // 비밀번호 암호화
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        userDto.setUserPassword(passwordEncoder.encode(userDto.getUserPassword()));

        userRepository.save(userDto.toEntity());
        System.out.println("회원가입 성공! - DB 저장 성공");
        return true;
    }

    // UserDetailsService 인터페이스에는 DB에서 유저 정보를 불러오는 중요한 메소드가 존재 - loadUserByUsername
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        // UserDetails: Spring Security에서 사용자의 정보를 담는 인터페이스는 UserDetails 인터페이스
        // 이 인터페이스를 구현하게 되면 Spring Security에서 구현한 클래스를 사용자 정보로 인식하고 인증 작업을 함
        // 쉽게 말하면 UserDetails 인터페이스는 VO 역할 - VO는 DTO와 동일한 개념이지만 read only 속성

        // Optional<T> 클래스는 Integer나 Double 클래스처럼 'T'타입의 객체를 포장해 주는 래퍼 클래스(Wrapper class)
        // Optional 객체를 사용하면 예상치 못한 NullPointerException 예외를 제공되는 메소드로 간단히 회피
        Optional<UserEntity> userEntityWrapper = userRepository.findByUserId(userId);

        // Optional 객체에 저장된 값이 null이면, NoSuchElementException 예외
        // 따라서 get() 메소드를 호출하기 전에 isPresent() 메소드를 사용하여 Optional 객체에 저장된 값이 null인지 아닌지를 먼저 확인한 후 호출

        if(userEntityWrapper.isPresent()) {
            // get() 메소드를 사용하면 Optional 객체에 저장된 값에 접근
            UserEntity userEntity = userEntityWrapper.get();

            List<GrantedAuthority> authority = new ArrayList<>();
            // 일단 권한은 ADMIN으로..
            authority.add(new SimpleGrantedAuthority(UserRole.ADMIN.getValue()));

            // return은 SpringSecurity에서 제공하는 UserDetails를 구현한 User를 반환
            return new User(userEntity.getUserId(), userEntity.getUserPassword(), authority);
        }

        return null;
    }
}
