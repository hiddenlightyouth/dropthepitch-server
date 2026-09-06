package kr.yuns.dropthepitchserver.analyze.event;

//새 작업이 만들어졌다는 사실만 전달한다.
//엔티티 대신 ID를 담는 이유: 이벤트를 받는 쪽이 다른 스레드라 영속성 컨텍스트가 다르기 때문이다.
public record ProjectCreatedEvent(Long projectId) { }
