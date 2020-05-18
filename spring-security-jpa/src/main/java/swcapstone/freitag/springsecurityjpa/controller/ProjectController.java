package swcapstone.freitag.springsecurityjpa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import swcapstone.freitag.springsecurityjpa.api.ObjectStorageApiClient;
import swcapstone.freitag.springsecurityjpa.domain.dto.ProjectDto;
import swcapstone.freitag.springsecurityjpa.service.AuthorizationService;
import swcapstone.freitag.springsecurityjpa.service.CollectionProjectService;
import swcapstone.freitag.springsecurityjpa.service.ProjectService;
import swcapstone.freitag.springsecurityjpa.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.List;

@RestController
public class ProjectController {

    @Autowired
    ProjectService projectService;
    @Autowired
    ObjectStorageApiClient objectStorageApiClient;
    @Autowired
    AuthorizationService authorizationService;
    @Autowired
    UserService userService;


    @RequestMapping("/api/project/create")
    public void createCollectionProject(HttpServletRequest request, HttpServletResponse response) {

        if(authorizationService.isAuthorized(request)) {
            String userId = authorizationService.getUserId(request);
            int howManyProjects = projectService.howManyProjects(userId) + 1;

            // 버킷 생성
            String bucketName = userId+howManyProjects;
            if(objectStorageApiClient.putBucket(bucketName)) {
                // 버킷 생성되면 수집 프로젝트 생성에 필요한 사용자 입력 필드와 함께 디비 저장
                projectService.createProject(request, userId, bucketName, response);
            }
        }

    }

    @RequestMapping(value = "/api/project/upload/example", method = RequestMethod.POST)
    public void uploadExampleData(HttpServletRequest request, @RequestParam("file") MultipartFile file,
                                  HttpServletResponse response) throws Exception {

        if(authorizationService.isAuthorized(request)) {
            String fileName = file.getOriginalFilename();
            String bucketName = request.getHeader("bucketName");

            File destinationFile = new File("/Users/woneyhoney/Desktop/files/" + fileName);
            // MultipartFile.transferTo() : 요청 시점의 임시 파일을 로컬 파일 시스템에 영구적으로 복사하는 역할을 수행
            file.transferTo(destinationFile);

            String exampleContent = objectStorageApiClient.putObject(bucketName, destinationFile);
            String userId = authorizationService.getUserId(request);

            // 예시 데이터 object의 Etag를 exampleContent로 지정하고 cost 설정하고 헤더에 붙이기
            if(projectService.setExampleContent(userId, exampleContent, response)) {
                // 수집 프로젝트는 예시 데이터 업로드 성공하면 바로 결제할 수 있도록 cost 계산해서 헤더에 쓰기
                if(projectService.isCollection(userId))
                    projectService.setCost(userId, response);

                // System.out.println("status: 없음 - 결제만 하면 됨. 그 외 프로젝트 생성 작업은 모두 완료");
                return;
            }

            // System.out.println("status: 없음 - 예시 데이터 Object Storage 업로드 실패");
        }
    }


    @RequestMapping(value = "/api/project/upload/labelling")
    public void uploadLabellingData(HttpServletRequest request, @RequestParam("file") List<MultipartFile> files,
                                    HttpServletResponse response) {

        if(authorizationService.isAuthorized(request)) {

        }
    }


    // 프로젝트 검색 결과 반환
    // workType, dataType, subject, difficulty
    @RequestMapping(value = "/api/project/list")
    public List<ProjectDto> getCollectionSearchResults(HttpServletRequest request, HttpServletResponse response) {

        if(authorizationService.isAuthorized(request)) {
            return projectService.getSearchResults(request, response);
        }

        return null;
    }


    // 오픈 뱅킹 결제
    // 결제 완료되면 status 없음 -> 진행중 변경할 것
    @RequestMapping(value = "/api/project/account/payment")
    public void payInAccount(HttpServletRequest request, HttpServletResponse response) {
        if(authorizationService.isAuthorized(request)) {

            String userId = authorizationService.getUserId(request);
            int cost = collectionProjectService.getCost(userId);

            if(userService.accountPayment(userId, cost, response)) {
                collectionProjectService.setStatus(userId, response);
            }

        }
    }

    // 사용자 포인트로 결제
    // 결제 완료되면 status 없음 -> 진행중 변경할 것
    @RequestMapping(value = "/api/project/point/payment")
    public void payInPoints(HttpServletRequest request, HttpServletResponse response) {
        if(authorizationService.isAuthorized(request)) {

            String userId = authorizationService.getUserId(request);
            int cost = projectService.getCost(userId);

            if(userService.pointPayment(userId, cost, response)) {
                projectService.setStatus(userId, response);
            }

        }
    }
}
