// backend/src/main/java/org/example/backend/domain/valueobject/Money.java
package org.example.backend.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;

/**
 * Money 值对象 - 修复版本
 * 
 * 关键修复：
 * 1. 添加了必要的JPA注解确保正确映射
 * 2. 修复了构造函数问题
 * 3. 添加了验证逻辑
 */
@Embeddable
public class Money {
    
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "currency", nullable = false, length = 3)
    private String currencyCode;
    
    // JPA需要的默认构造函数
    protected Money() {
        this.amount = BigDecimal.ZERO;
        this.currencyCode = "CNY";
    }
    
    private Money(BigDecimal amount, String currencyCode) {
        validateAmount(amount);
        validateCurrencyCode(currencyCode);
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.currencyCode = currencyCode;
    }
    
    /**
     * 创建货币金额
     */
    public static Money of(BigDecimal amount, String currencyCode) {
        return new Money(amount, currencyCode);
    }
    
    /**
     * 创建货币金额 - 便利方法
     */
    public static Money of(double amount, String currencyCode) {
        return of(BigDecimal.valueOf(amount), currencyCode);
    }
    
    /**
     * 创建人民币金额
     */
    public static Money cny(BigDecimal amount) {
        return of(amount, "CNY");
    }
    
    public static Money cny(double amount) {
        return of(amount, "CNY");
    }
    
    /**
     * 创建零金额
     */
    public static Money zero(String currencyCode) {
        return of(BigDecimal.ZERO, currencyCode);
    }
    
    public static Money zeroCny() {
        return zero("CNY");
    }
    
    // ========== 货币运算方法 ==========
    
    /**
     * 加法运算
     */
    public Money add(Money other) {
        checkSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currencyCode);
    }
    
    /**
     * 减法运算
     */
    public Money subtract(Money other) {
        checkSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currencyCode);
    }
    
    /**
     * 乘法运算
     */
    public Money multiply(BigDecimal multiplier) {
        if (multiplier == null) {
            throw new IllegalArgumentException("乘数不能为null");
        }
        return new Money(this.amount.multiply(multiplier), this.currencyCode);
    }
    
    public Money multiply(double multiplier) {
        return multiply(BigDecimal.valueOf(multiplier));
    }
    
    /**
     * 除法运算
     */
    public Money divide(BigDecimal divisor) {
        if (divisor == null || divisor.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("除数不能为null或零");
        }
        return new Money(this.amount.divide(divisor, 2, RoundingMode.HALF_UP), this.currencyCode);
    }
    
    public Money divide(double divisor) {
        return divide(BigDecimal.valueOf(divisor));
    }
    
    /**
     * 绝对值
     */
    public Money abs() {
        return this.amount.compareTo(BigDecimal.ZERO) < 0 ? 
            new Money(this.amount.abs(), this.currencyCode) : this;
    }
    
    /**
     * 取负值
     */
    public Money negate() {
        return new Money(this.amount.negate(), this.currencyCode);
    }
    
    // ========== 比较方法 ==========
    
    /**
     * 是否为正数
     */
    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * 是否为负数
     */
    public boolean isNegative() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }
    
    /**
     * 是否为零
     */
    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }
    
    /**
     * 大于比较
     */
    public boolean isGreaterThan(Money other) {
        checkSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }
    
    /**
     * 小于比较
     */
    public boolean isLessThan(Money other) {
        checkSameCurrency(other);
        return this.amount.compareTo(other.amount) < 0;
    }
    
    // ========== 验证方法 ==========
    
    private static void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("金额不能为null");
        }
        // 可以添加额外的业务规则，比如金额范围限制
        if (amount.scale() > 2) {
            throw new IllegalArgumentException("金额精度不能超过2位小数");
        }
    }
    
    private static void validateCurrencyCode(String currencyCode) {
        if (currencyCode == null || currencyCode.trim().isEmpty()) {
            throw new IllegalArgumentException("货币代码不能为空");
        }
        
        if (!currencyCode.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("货币代码必须是3位大写字母");
        }
        
        try {
            Currency.getInstance(currencyCode);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效的货币代码: " + currencyCode);
        }
    }
    
    private void checkSameCurrency(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("货币金额不能为null");
        }
        if (!this.currencyCode.equals(other.currencyCode)) {
            throw new IllegalArgumentException(
                String.format("货币类型不匹配: %s vs %s", this.currencyCode, other.currencyCode)
            );
        }
    }
    
    // ========== Getter方法 ==========
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public String getCurrencyCode() {
        return currencyCode;
    }
    
    /**
     * 获取Currency对象
     */
    public Currency getCurrency() {
        return Currency.getInstance(currencyCode);
    }
    
    // ========== Object方法重写 ==========
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Money money = (Money) obj;
        return Objects.equals(amount, money.amount) &&
               Objects.equals(currencyCode, money.currencyCode);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(amount, currencyCode);
    }
    
    @Override
    public String toString() {
        return String.format("%s %s", amount.toPlainString(), currencyCode);
    }
    
    /**
     * 格式化显示 - 带货币符号
     */
    public String toDisplayString() {
        Currency currency = getCurrency();
        return String.format("%s %s", currency.getSymbol(), amount.toPlainString());
    }
}