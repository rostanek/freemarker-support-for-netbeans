<#assign name=value>
or
<#assign name1=value1 name2=value2 ... nameN=valueN>
or
<#assign same as above... in namespacehash>
or
<#assign name>
  capture this
</#assign>
or
<#assign name in namespacehash>
  capture this
</#assign>

<#if condition>
  ...
<#elseif condition2>
  ...
<#elseif condition3>
  ...
...
<#else>
  ...
</#if>

<#import "/libs/mylib.ftl" as my>

<#list seq as x>
  ${x_index + 1}. ${x}<#if x_has_next>,</#if>
</#list>

<@html_escape>
  a < b
  Romeo & Juliet
</@html_escape>  

<#-- comment -->