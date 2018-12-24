# APIJSONParser

> 参考项目[APIJSON](https://github.com/TommyLemon/APIJSON)设计的一款编译SQL编译器框架，现已初步实现APIJSON格式的JSON到SQL的转换

详细文档，计划2019年1月1日左右更新

## 未来

1. 现在的程序是写死的，处理逻辑能以链的形式组合就好了

@pipe("name")

如果能够自动装配的话，最好有一个

2. 使用Mybatis别名的技巧来实现自动装配

```json
{
 "T":{
    "@column":"a,b,c"
  },
 "T2":{
    "@parent":"T"
    "@column":"d,e,f"
 }  
} 
```

查询结果：

```json
  {
   "T":{
     "a":"a",
     "b":"b",
     "c":"c",
     "T2":{
       "d":"d",
       "e":"e",
       "f":"f"
     }
   }
  }  
```

3. 表，字段的黑白名单放出来，作为

4. 权限的控制通过

  