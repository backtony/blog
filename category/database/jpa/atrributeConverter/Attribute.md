## AttributeConverter
AttributeConverter는 주로 다음과 같은 상황에서 사용됩니다.

* JPA가 지원하지 않는 타입을 매핑
* 두 개 이상의 속성을 갖는 밸류 타입을 한 개 칼럼에 매핑

<br>

## JPA가 지원하지 않는 타입을 매핑
```java
public interface AttributeConverter<X,Y> {
    public Y convertToDatabaseColumn (X attribute);
    public X convertToEntityAttribute (Y dbData);
}
```
* X : 엔티티의 속성에 대응하는 타입
* Y : DB에 대응하는 타입
* convertToDatabaseColumn 
  * 엔티티의 X 타입 속성을 Y 타입의 DB 데이터로 변환합니다. 
  * 엔티티 속성을 DB에 반영할 때 사용됩니다.
* convertToEntityAttribute
  * Y 타입으로 읽은 DB 데이터를 엔티티의 X 타입의 속성으로 변환합니다.
  * 엔티티 조회시 DB에서 읽어온 데이터를 엔티티의 속성에 반영할 떄 사용됩니다.

```java
public enum DirectoryType {
    DEFAULT, CUSTOM, UNKNOWN
}

@Converter
public class DirectoryTypeConverter implements AttributeConverter<DirectoryType, String> {
    @Override
    public String convertToDatabaseColumn(DirectoryType attribute) {
        return attribute.name();
    }

    @Override
    public DirectoryType convertToEntityAttribute(String dbData) {
        return Arrays.stream(DirectoryType.values()).filter(constraintSet -> constraintSet.name().equals(dbData))
            .findAny().orElse(DirectoryType.UNKNOWN);
    }
}

@Entity
public class ServiceDirectory {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long no;


    @Convert(converter = DirectoryTypeConverter.class)
    private DirectoryType directoryType;
}
```
<br>

## 두 개 이상의 속성을 갖는 밸류 타입을 한 개 컬럼에 매핑
이런 경우는 흔치 않지만 이렇게 할 수 있다 정도로만 알아두면 될 것 같습니다.

```java
public class Money {
    private Double value;
    private String currency;

    public Money(Double value, String currency) {
        this.value = value;
        this.currency = currency;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public String toString() {
        return value.toString() + currency;
    }
}
```
* Money 타입을 DB에 보관할 때 “1000KRW”이나 “100USD”와 같은 문자열로 저장다고 가정합니다.
* Money를 한 개의 칼럼에 매핑하므로 @Embeddable을 사용할 수 없습니다.

```java
@Converter(autoApply = true)
public class MoneyConverter implements AttributeConverter<Money, String> {

    @Override
    public String convertToDatabaseColumn(Money attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.toString();
    }

    @Override
    public Money convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        } else {
            String value = dbData.substring(0, dbData.length() - 3);
            String currency = dbData.substring(dbData.length() - 3);
            return new Money(Double.valueOf(value), currency);
        }
    }
}
```
autuApply 설정을 true로 설정했으므로 JPA 프로바이더는 MoneyConverter를 자동으로 적용합니다.