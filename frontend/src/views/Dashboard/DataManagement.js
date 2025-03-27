import React, { useState, useEffect } from "react";
import axios from "axios"; // Import axios for making API requests
import { Card, Table, Select, Typography, Dropdown, Menu } from "antd";

const { Title } = Typography;
const { Option } = Select;

export default function IncomeExpensePage() {
  const [incomeCategories, setIncomeCategories] = useState([]); // State to hold income categories data
  const [outcomeCategories, setOutcomeCategories] = useState([]); // State to hold outcome categories data
  const [selectedCategory, setSelectedCategory] = useState("All");
  const [selectedOutcome, setSelectedOutcome] = useState("All");

  // Fetch income categories data from backend on component mount
  useEffect(() => {
    // Fetch income categories from the income API
    axios.get("http://localhost:8080/api/income-categories")
      .then(response => {
        setIncomeCategories(response.data); // Set the income categories data to state
      })
      .catch(error => {
        console.error("There was an error fetching the income categories!", error);
      });

    // Fetch outcome categories from the outcome API
    axios.get("http://localhost:8080/api/outcome-categories")
      .then(response => {
        setOutcomeCategories(response.data); // Set the outcome categories data to state
      })
      .catch(error => {
        console.error("There was an error fetching the outcome categories!", error);
      });
  }, []);

  // Find all top-level categories (parentCategoryId is null)
  const topIncomeCategories = incomeCategories.filter(category => category.parentCategoryId === null);
  const topOutcomeCategories = outcomeCategories.filter(category => category.parentCategoryId === null);

  // Function to get subcategories for a given parentCategoryId
  const getSubcategories = (categories, parentCategoryId) => {
    return categories.filter(category => category.parentCategoryId === parentCategoryId);
  };

  // Handle category selection change for Income
  const handleIncomeCategoryChange = value => {
    setSelectedCategory(value);
  };

  // Handle category selection change for Outcome
  const handleOutcomeCategoryChange = value => {
    setSelectedOutcome(value);
  };

  // Filter categories based on selected category (parent category or subcategory) for Income
  const filteredIncomeCategories = selectedCategory === "All"
    ? incomeCategories
    : incomeCategories.filter(category => 
        category.categoryId === selectedCategory || category.parentCategoryId === selectedCategory
    );

  // Filter categories based on selected category (parent category or subcategory) for Outcome
  const filteredOutcomeCategories = selectedOutcome === "All"
    ? outcomeCategories
    : outcomeCategories.filter(category => 
        category.categoryId === selectedOutcome || category.parentCategoryId === selectedOutcome
    );

  // Render dropdown menu for parent categories with subcategories shown on hover
  const renderCategoryDropdown = (parent, subcategories) => {
    return (
      <Menu>
        {subcategories.map(sub => (
          <Menu.Item key={sub.categoryId}>
            {sub.name}
          </Menu.Item>
        ))}
      </Menu>
    );
  };

  return (
    <div style={{ padding: "20px", maxWidth: "800px", margin: "auto" }}>
      <Title level={2}>Income & Expense Records</Title>

      {/* Income Category filter */}
      <Select
        value={selectedCategory}
        onChange={handleIncomeCategoryChange}
        style={{ width: 200, marginBottom: 20, marginRight: 10 }}
      >
        <Option key="All" value="All">All Income</Option>
        {topIncomeCategories.map(parent => {
          // Get subcategories for this parent
          const subcategories = getSubcategories(incomeCategories, parent.categoryId);
          return (
            <Option key={parent.categoryId} value={parent.categoryId}>
              <Dropdown overlay={renderCategoryDropdown(parent, subcategories)} trigger={['hover']}>
                <a>{parent.name}</a>
              </Dropdown>
            </Option>
          );
        })}
      </Select>

      {/* Outcome Category filter */}
      <Select
        value={selectedOutcome}
        onChange={handleOutcomeCategoryChange}
        style={{ width: 200, marginBottom: 20 }}
      >
        <Option key="All" value="All">All Outcome</Option>
        {topOutcomeCategories.map(parent => {
          // Get subcategories for this parent
          const subcategories = getSubcategories(outcomeCategories, parent.categoryId);
          return (
            <Option key={parent.categoryId} value={parent.categoryId}>
              <Dropdown overlay={renderCategoryDropdown(parent, subcategories)} trigger={['hover']}>
                <a>{parent.name}</a>
              </Dropdown>
            </Option>
          );
        })}
      </Select>

      {/* Show filtered categories for Income */}
      <RecordTable title="Income Records" records={filteredIncomeCategories} />

      {/* Show filtered categories for Outcome */}
      <RecordTable title="Outcome Records" records={filteredOutcomeCategories} />
    </div>
  );
}

// Record Table Component
function RecordTable({ title, records }) {
  const columns = [
    { title: "Category", dataIndex: "name", key: "name" },
    { title: "Type", dataIndex: "type", key: "type" },
  ];

  return (
    <Card title={title} style={{ marginBottom: 20 }}>
      <Table dataSource={records} columns={columns} rowKey="categoryId" pagination={false} />
    </Card>
  );
}
