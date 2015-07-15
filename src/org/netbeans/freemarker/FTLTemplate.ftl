<#ftl strip_whitespace = true>

<#setting boolean_format=computer>
<#import "/libs/mylib.ftl" as my>

<#assign charset="UTF-8">
<#assign title="Example">
<#assign content>
This is content
</#assign>
<!DOCTYPE html>
<html>
	<head>
		<title>${title}</title>
		<meta charset="${charset}">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
	</head>
	<body><#if content??>
		<div>${content}</div>
		<#else>
		<div>No content</div>
		</#if>
		<@my.function>parameter</@my.function>
		

	</body>
</html>
