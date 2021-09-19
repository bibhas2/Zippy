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

## Attribute Binding
Attributes that need to show a dynamic value must be prefixed with ":". This prefix is stripped out in the output.

```java
var templateStr = "<div><p id='para-1' class='footer' :first-name='name'>Hello</p></div>";
var template = Zippy.compile(templateStr);

Map<String, Object> ctx = new HashMap<>();

ctx.put("name", "Daffy Duck");

var out = Zippy.evalAsString(template, ctx);
```

This will produce an output like this.

```html
<div>
    <p id='para-1' class='footer' first-name="Daffy Duck">Hello</p>
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

Avoid using ``v-for`` and ``v-if`` for the same element. If you do then ``v-if`` may not have access to
the loop variable defined in ``v-for``. This is because the DOM parser does not guarantee
the ordering of the attributes and ``v-if`` may be processed before ``v-for``.
