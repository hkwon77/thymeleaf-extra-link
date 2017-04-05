# thymeleaf-extra-link

request로 넘어오는 query string을 모두 링크 생성시 자동으로 붙여주는 Thymeleaf 확장

## Dialect Engine에 등록

Spring boot 를 사용한다면 `IProcessorDialect` 클래스를 구현한 놈은 자동으로 등록해주기 때문에 별다른 설정 없어 `Bean`을 추가해주면 된다.

```java
@Configuration
public class ThymeleafConfig {
	@Bean
	public ExtraLinkDialect extraLinkDialect() {
		return new ExtraLinkDialect("UTF-8");
	}
}
```

혹은 `SpringTemplateEngine` 생성 시 addDialect로 추가해 주면된다.

```
@Bean
public SpringTemplateEngine templateEngine() {
    SpringTemplateEngine templateEngine = new SpringTemplateEngine();

    templateEngine.setEnableSpringELCompiler(true);
    templateEngine.setTemplateResolver(templateResolver());
    templateEngine.setMessageSource(messageSource);
    templateEngine.addDialect(new LayoutDialect());
    templateEngine.addDialect(new SpringDataDialect());
    templateEngine.addDialect(new ExtraLinkDialect("UTF-8"));

    return templateEngine;
}
```

## 사용 방법

```html
http://localhost:8080/users?pageNum=2&query=검색어&test=1%26encoding
```

url이 위와 같다면

```html
<a th:link="@{/users}">링크</a>
```

실제 생성 attribute

```html
<a href="/users?pageNum=2&amp;query=%EA%B2%80%EC%83%89%EC%96%B4&amp;test=1%26encoding">링크</a>
```

파라미터가 있고 중복인 경우

```html
<a th:link="@{/users(pageNum=3)}">링크</a>
```

```html
<a href="/users?pageNum=3&amp;query=%EA%B2%80%EC%83%89%EC%96%B4&amp;test=1%26encoding">링크</a>
```
