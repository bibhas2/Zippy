Zippy is a minimalistic templating system. It's main use is to generate HTML for emails. It uses
the template syntax of Vue.js for simplicity and familiarity.

## Example

```java
var templateStr = "<div>{{name}}</div>";
//Compile the template. This is a one-time thing.
var template = Zippy.compile(templateStr);

//Evaluate the template.
var ctx = new HashMap<String, Object>();

ctx.put("name", "Daffy Duck");

var out = Zippy.evalAsString(template, ctx);
```

This will produce an output like this.

```html
<div>Daffy Duck</div>
```

## Main Features

- Minimal external dependency.
- Rich expression language using the Apache Commons JEXL library.
- Sufficient to generate good quality dynamic HTML for emails. This may be a more
lightweight choice than Thymeleaf, Velocity etc.

## Using Zippy

Add this dependency to your ``pom.xml``.

```xml
<dependency>
    <groupId>io.github.bibhas2</groupId>
    <artifactId>zippy</artifactId>
    <version>1.1.2</version>
</dependency>
```

## Compiling a Template
You can supply the template as a plain string.

```java
var templateStr = "<div>{{name}}</div>";
//Compile the template.
var template = Zippy.compile(templateStr);
```

A common usage is to load a template file from the classpath.

```java
var template = Zippy.compileResource("my-template.html");
```

Note that a template must be a valid XML. HTML is a relaxed form of XML. A valid HTML is not necessarily a valid XML. For example the HTML below is not a valid XML because the attribute values are not quoted.

```html
<d id=d1 class=footer>Hello</div>
```

## Evaluating a Template
Once a template is compiled it can be evaluated many times from multiple threads. You are encouraged to compile templates only once for better performance.

To evaluate a template you must construct a context ``HashMap``. This map will have all the variables referred to by the expressions in the template.

```java
var ctx = new HashMap<String, Object>();

ctx.put("firstName", "Daffy");
ctx.put("lastName", "Duck");
```

You can evaluate the template and obtain a result ``String``.

```java
var template = Zippy.compile("<div>Hello {{firstName}} {{lastName}}.</div>");
String out = Zippy.evalAsString(template, ctx);
```

You can evaluate the template and obtain a result DOM ``Document``.

```java
var template = Zippy.compile("<div>Hello {{firstName}} {{lastName}}.</div>");
Document out = Zippy.eval(template, ctx);
```

## Attribute Binding
Attributes that need to show a dynamic value must be prefixed with ":". This prefix is stripped out in the output.

```java
var templateStr = "<div><p id='para-1' :class='theClass'>Hello</p></div>";
var template = Zippy.compile(templateStr);

Map<String, Object> ctx = new HashMap<>();

ctx.put("theClass", "footer");

var out = Zippy.evalAsString(template, ctx);
```

This will produce an output like this.

```html
<div>
    <p id="para-1" class="footer">Hello</p>
</div>
```

## Body Text
To output dynamic data in the element body, use the ``{{ }}`` syntax.

```java
var templateStr = "<div><p>Hello {{name}}!</p></div>";
var template = Zippy.compile(templateStr);

Map<String, Object> ctx = new HashMap<>();

ctx.put("name", "Daffy Duck");

var out = Zippy.evalAsString(template, ctx);
```

This will produce an output like this.

```html
<div>
    <p>Hello Daffy Duck!</p>
</div>
```

## Conditional Output
To conditionally evaluate an element and all of its children use ``v-if``.

Example template:

```html
<div>
    <p v-if="name == 'Bugs Bunny'">Hello {{name}}!</p>
</div>
```

```java
var templateStr = ...;
var template = Zippy.compile(templateStr);

Map<String, Object> ctx = new HashMap<>();

ctx.put("name", "Daffy Duck");

var out = Zippy.evalAsString(template, ctx);
```

This will produce an output like this.

```html
<div>
</div>
```

## Looping

Use ``v-for`` to loop over a ``List`` or array and repeatedly generate elemenets.


```java
var templateStr = "<div><p v-for='name in nameList' :first-name='name'>Hello {{name}}</p></div>";

//Compile the template. This needs to be done only once.
var template = Zippy.compile(templateStr);

//Evaluate the template. This can be done many times.

Map<String, Object> ctx = new HashMap<>();
var nameList = Arrays.asList("Daffy", "Bugs");

ctx.put("nameList", nameList);

var out = Zippy.evalAsString(template, ctx);
```

This will produce an output like this.

```html
<div>
    <p first-name="Daffy">Hello Daffy</p>
    <p first-name="Bugs">Hello Bugs</p>
</div>
```

You can use a ``List`` or an array of objects with ``v-for``. Array of promitive types are not
supported.

You can use ``v-for`` and ``v-if`` for the same element. Example:

```html
<html>
    <body>
        <div v-for="name in nameList" v-if="name == 'Daffy'">
            <p>{{name}}</p>
        </div>
    </body>
</html>
```

## Inserting Shared Content
You can insert the content from one template inside the content from another template. This is useful to create shared content used by multiple templates.

Example:

```java
//This is the shared content
var innerTemplate = Zippy.compile("<p id='p1'>Hello</p>");
var innerDoc = Zippy.eval(innerTemplate, ctx);

//Use the shared content
var templateStr = "<div>{{greeting}}</div>";
var template = Zippy.compile(templateStr);
//You must store the shared DOM Document
//to have it inserted.
ctx.put("greeting", innerDoc);

var out = Zippy.eval(template, ctx);
```

This will produce the following content.

```html
<div>
    <p id="p1">Hello</p>
</div>
```