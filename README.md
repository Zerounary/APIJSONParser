# APIJSONParser

> 参考项目[APIJSON](https://github.com/TommyLemon/APIJSON)设计的一款编译SQL编译器框架，现已实现APIJSON格式的JSON到SQL的转换

## 简介
在APIJSON程序中，主要分三步：权限判断，解析生成SQL，包装成制定格式的响应

APIJSONParser是其中**解析生成SQL**过程的一个工具类，提供将制定格式的JSON对象转化生成一条标准的SQL，如果你想自定义格式，底层的框架也完全支持你进行自由扩展。

假设一个JSON是这样的：

```json
{
    "Product":{
        "id":"1"
    }
}
```

将上述的JSON解析到一个叫`obj`的`JSONObject`中，经过以下代码：

```java
APIJSONProvider apijsonProvider = new APIJSONProvider(obj); //一个APIJSON解析器拿到一个json对象
apijsonProvider.setStatementType(StatementType.SELECT); //当前为查询模式，还支持新增，修改，删除
SQLExplorer builder = new SQLExplorer(apijsonProvider); //装载APIJSON解析器
System.out.println(builder.getSQL());//拿到SQL
```

之后会打印这样一句SQL

```sql
SELECT Product.*
FROM Product Product
WHERE (Product.id = '1')
```

通过对比，我们很容易发现，其中的奥秘。JSON中`Product`是指表名，而其中的`id`则是它的过滤条件。这里你可能会有一些疑惑，比如注入之类的安全问题。解析器的在进行解析的时候，会进行严格的格式检查，诚然这将会牺牲一些效率和SQL本身的灵活性。



如果你觉得这里的别名很怪异，想自己定义，你可以这样：

```json
{
    "Product:p":{
        "id":"1"
    }
}
```

结果：

```sql
SELECT p.*
FROM Product p
WHERE (p.id = '1')
```





这里我们查询出来的是`id`为1的数据的所有字段，如果我们想要部分字段怎么办？

```json
{
    "Product:p":{
        "@column":"id,code,name",
        "id":"1"
    }
}
```

结果：

```sql
SELECT p.id, p.code, p.name
FROM Product p
WHERE (p.id = '1')
```





倘若，你想要给表加别名一样给字段也加个别名

```json
{
    "Product:p":{
        "@column":"id,code,name:productName",
        "id":"1"
    }
}
```

结果：

```sql
SELECT p.id, p.code, p.name as productName
FROM Product p
WHERE (p.id = '1')
```





到现在为止，我们都是以`id`为1作为过滤条件的，其他还有很多过滤方法。

对于一个数值字段，我们要找出价格（price）300元以上的商品，我们可以这样

```json
{
    "Product:p":{
        "@column":"id,code,name,price",
        "price{}":">300"
    }
}
```

结果：

```sql
SELECT p.id, p.code, p.name, p.price
FROM Product p
WHERE (p.price > 300)
```





这里的`{}`表示你查询的是一个范围，如果你有多个范围。除了这里演示的`>`，自然你也可以使用`<`，`<=`，`>`，`>=`，`=`

```json
{
    "Product:p":{
        "@column":"id,code,name,price",
        "price{}":">300,<1200"
    }
}
```

结果：

```sql
SELECT p.id, p.code, p.name, p.price
FROM Product p
WHERE (p.price > 300 AND p.price < 1200)
```





现在我们查询的范围是，300 < price < 1200，如果想要找，price <= 300或者price >= 1200的数据。

```json
{
    "Product:p":{
        "@column":"id,code,name,price",
        "price|{}":">300,<1200"
    }
}
```

结果：

```sql
SELECT p.id, p.code, p.name, p.price
FROM Product p
WHERE (p.price > 300 OR p.price < 1200)
```





`{}`的另外一种用法是IN，只需要你`{}`的字段的值的类型的JSONArray。

例如，我们的商品`Product`有一个品牌的字段`brand_id`，这是一个品牌表（Brand）的外键字段，我们想选择某一部分的品牌的商品出来

```json
{
    "Product:p":{
        "@column":"id,code,name,price",
        "brand_id{}": [1,4,5]
    }
}
```

结果：

```sql
SELECT p.id, p.code, p.name, p.price
FROM Product p
WHERE (p.brand_id IN (1, 4, 5))
```





如果要排除某些品牌，你可以

```json
{
    "Product:p":{
        "@column":"id,code,name,price",
        "brand_id!{}": [1,4,5]
    }
}
```

结果：

```sql
SELECT p.id, p.code, p.name, p.price
FROM Product p
WHERE (p.brand_id NOT IN (1, 4, 5))
```

此功能同样支持文本字段





如果想查看商品名称字段`name`包含某一子串`双11`的商品

```json
{
    "Product:p":{
        "@column":"id,code,name,price",
        "name~": "双11"
    }
}
```

结果：

```sql
SELECT p.id, p.code, p.name, p.price
FROM Product p
WHERE (p.name LIKE '%双11%')
```





如果你想自己调控`%`，也可以直接使用。

```json
{
    "Product:p":{
        "@column":"id,code,name,price",
        "name$": "%双11"
    }
}
```

结果：

```sql
SELECT p.id, p.code, p.name, p.price
FROM Product p
WHERE (p.name LIKE '%双11')
```

`%`放哪里，你可以自己决定。





想使用正则的话

```json
{
    "Product:p":{
        "@column":"id,code,name,price",
        "code?": "^[0-9]+$"
    }
}
```

结果：

```sql
SELECT p.id, p.code, p.name, p.price
FROM Product p
WHERE ( regexp_like(p.code,'^[0-9]+$'))
```

介于不同的数据库，函数名有一些区别，可按实际情况修改。`APIJSONProvider`类`345`行，进行修改。





如果你想要排序，你可以这样

```json
{
    "Product:p":{
        "@column":"id,code,name,price",
        "name~": "双11",
        "@orders": "price-"
    }
}
```

结果：

```sql
SELECT p.id, p.code, p.name, p.price
FROM Product p
WHERE (p.name LIKE '%双11%')
ORDER BY p.price DESC
```

`-`表示降序，`+`表示增序。以上查询的是双11商品价格从高到底的列表





有时我们也要进行分组操作，比如找出双11商品中的最贵的商品

```json
{
    "Product:p":{
        "@column":"name,max(price):max_price",
        "name~": "双11",
        "@group": "name"
    }
}
```

结果：

```sql
SELECT p.name, max(p.price) as max_price
FROM Product p
WHERE (p.name LIKE '%双11%')
GROUP BY p.name
```

目前允许的函数没有进行控制，所有的单一参数的函数都可以使用。

单表查询的内容基本介绍完了，接下来是多表查询。

现在有两张表，零售表`m_retail`，零售的店铺`m_retail.c_store_id`外键关联到`c_store`表。

第一种是使用`id@`

```json
{
  "m_retail:r":{
      "@column":"id,docno",
      "id": 18
    },
  "c_store:s":{
    "id@":"/r/c_store_id",
    "@column":"code,name"
  }
}
```

结果：

```sql
SELECT r.id, r.docno, s.code as "s.code", s.name as "s.name"
FROM m_retail r, c_store s
WHERE (r.id = 18 AND r.c_store_id = s.id)
```

如果不使用`id@`

```json
{
    "[]": {
        "c_store": {
            "@column": "code,name",
            "code": "C86L"
        },
        "m_retail:r": {
            "@column": "id,docno"
        }
    },
    "join": {
        "@innerJoin": [
            "c_store.id=r.c_store_id"
        ]
    }
}
```

结果:

```sql
SELECT r.id, r.docno, c_store.code, c_store.name
FROM m_retail r
INNER JOIN c_store ON c_store.id=r.c_store_id
WHERE (c_store.code = 'C86L')
```



其他几种join的使用方法和这里演示的innerJoin是一样的。 

现在支持的join有：

@innerJoin
@leftOuterJoin
@rightOuterJoin
@join
@outerJoin 





 


## 速查

> 速查中的示例，都是最简单的实例，更新，删除的功能中的过滤条件的使用和查询相同，具体使用参考上述说明



一个叫`test_apijson`的表



| ID   | CODE | NAME   |
| ---- | ---- | ------ |
| 1    | A001 | 浦东区 |
| 2    | B001 | 徐汇区 |
| 3    | C001 | 普陀区 |



### 查询


查询`A001`的名称



```json
{
    "test_apijson":{
        "@column":"name",
        "code":"A001"
    }
}
```



### 新增


新增一行如ID=1的数据



```json
{
    "test_apijson":{
        "code":"A001",
        "name":"浦东区"
    }
}
```



ID是自动增长的，所以不需要再给ID赋值。

### 修改


修改代码`B001`为`A002`



```json
{
    "test_apijson":{
        "@code":"A002",
        "code": "B001"
    }
}
```





### 删除


删除`A002`



```json
{
    "test_apijson":{
        "code": "A002"
    }
}
```



## 权限

### 表权限

表白名单：`APIJSONProvider.getTableWhiteList()`

表黑名单：`APIJSONProvider.getTableBlackList()`

表的黑白名单的使用



```java
apijsonProvider.getTableBlackList().add("Retail");
```

`Retail`是表名



### 字段权限

字段白名单：`APIJSONProvider.getColumnWhiteList()`

字段黑名单：`APIJSONProvider.getColumnBlackList()`

字段的黑白名单的使用



```java
apijsonProvider.getColumnWhiteList().add("retail.*");
apijsonProvider.getColumnWhiteList().add("retail.amt");
```

格式：`表名.字段名`，字段名支持通配符`*`

