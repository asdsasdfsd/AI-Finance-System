
import React, { useState } from 'react';
import ReactPaginate from 'react-paginate';

const incomeData = [
    {
        item: 'Pledges',
        currentMonth: 37110.00,
        previousMonth: 47962.32,
        yearToDate: 37110.00,
        recoveryFromJACM: null,
        actualYTD: 37110.00,
        budgetYTD: 36000.00,
        variance: 1110.00,
        fullYearBudget: 432000.00
    },
    {
        item: 'Donations',
        currentMonth: 21500.00,
        previousMonth: 23980.45,
        yearToDate: 21500.00,
        recoveryFromJACM: 1000.00,
        actualYTD: 22500.00,
        budgetYTD: 25000.00,
        variance: -2500.00,
        fullYearBudget: 300000.00
    },
    {
        item: 'Grants',
        currentMonth: 104500.00,
        previousMonth: 112000.00,
        yearToDate: 104500.00,
        recoveryFromJACM: 5000.00,
        actualYTD: 109500.00,
        budgetYTD: 100000.00,
        variance: 9500.00,
        fullYearBudget: 1200000.00
    },
    {
        item: 'Fundraising',
        currentMonth: 8000.00,
        previousMonth: 9800.00,
        yearToDate: 8000.00,
        recoveryFromJACM: null,
        actualYTD: 8000.00,
        budgetYTD: 15000.00,
        variance: -7000.00,
        fullYearBudget: 180000.00
    },
    {
        item: 'Sponsorships',
        currentMonth: 54000.00,
        previousMonth: 56000.00,
        yearToDate: 54000.00,
        recoveryFromJACM: 3000.00,
        actualYTD: 57000.00,
        budgetYTD: 55000.00,
        variance: 2000.00,
        fullYearBudget: 660000.00
    },
    {
        item: 'Membership Fees',
        currentMonth: 11800.00,
        previousMonth: 12000.00,
        yearToDate: 11800.00,
        recoveryFromJACM: null,
        actualYTD: 11800.00,
        budgetYTD: 12000.00,
        variance: -200.00,
        fullYearBudget: 144000.00
    }
];


const balanceSheetData = [
    {
        item: 'General Fund Reserve',
        balanceAsAt: 1037513.57,
        previousMonth: null,
        asAt31Dec24: null,
        currentYearSurplus: 0,
        total: 1037513.57,
    },
    {
        item: 'Other Funds',
        subItems: [
            { subItem: 'C.O.M.E. Funds', amount: 104693.33, previousMonth: null, asAt31Dec24: null },
            { subItem: 'Sinking Fund', amount: 1855.0, previousMonth: null, asAt31Dec24: null },
            { subItem: 'Reno Cap Fund', amount: 74560.35, previousMonth: null, asAt31Dec24: null },
            { subItem: 'FFE Cap Fund', amount: 75058.17, previousMonth: null, asAt31Dec24: null },
        ],
    },
    {
        item: 'Total Reserves and Funds',
        balanceAsAt: 1293680.42,
        previousMonth: null,
        asAt31Dec24: null,
        currentYearSurplus: 0,
        total: 1293680.42,
    },
    {
        item: 'Fixed Assets',
        subItems: [
            { subItem: 'Plant & Equipment', amount: 354616.2, previousMonth: null, asAt31Dec24: null },
            { subItem: 'Less Accumulated Depreciation', amount: -117837.93, previousMonth: null, asAt31Dec24: null },
        ],
    },
    {
        item: 'Current Assets',
        subItems: [
            { subItem: 'Sundry Debtors', amount: 15030.77, previousMonth: null, asAt31Dec24: null },
            { subItem: 'Deposits', amount: 80.0, previousMonth: null, asAt31Dec24: null },
            { subItem: 'Prepayments', amount: 16537.69, previousMonth: null, asAt31Dec24: null },
            { subItem: 'Fixed Deposit - HL Finance', amount: 836027.21, previousMonth: null, asAt31Dec24: null },
            { subItem: 'Cash & Bank Balances - DBS Bank', amount: 394588.36, previousMonth: null, asAt31Dec24: null },
        ],
    },
    {
        item: 'Liabilities',
        subItems: [
            { subItem: 'Sundry Creditors', amount: 1890.0, previousMonth: null, asAt31Dec24: null },
            { subItem: 'Accruals', amount: 187535.6, previousMonth: null, asAt31Dec24: null },
            { subItem: 'Contra', amount: 8778.28, previousMonth: null, asAt31Dec24: null },
            { subItem: 'Advance Receipts', amount: 7158.0, previousMonth: null, asAt31Dec24: null },
        ],
    },
    {
        item: 'Total Net Assets',
        balanceAsAt: 1293680.42,
        previousMonth: null,
        asAt31Dec24: null,
        currentYearSurplus: 0,
        total: 1293680.42,
    },
];


const itemsPerPage = 5;

export default function FinancialDashboard() {
    const [view, setView] = useState('income');
    const [pageByView, setPageByView] = useState({ income: 0, balance: 0 });

    const handleViewChange = (newView) => {
        setView(newView);
    };

    const handlePageClick = (selectedItem) => {
        setPageByView(prev => ({ ...prev, [view]: selectedItem.selected }));
    };

    const pageCount = Math.ceil(incomeData.length / itemsPerPage);
    const currentPage = pageByView[view];
    const currentData = incomeData.slice(currentPage * itemsPerPage, (currentPage + 1) * itemsPerPage);

    return (
        <div className="p-6">
            <div className="mb-6 flex items-center justify-between">
                <div className="flex gap-4">
                    <button
                        className={`px-5 py-2 text-sm rounded-full transition-all duration-300 ${view === 'income' ? 'bg-blue-600 text-white shadow-md' : 'bg-gray-200 text-gray-700'}`}
                        onClick={() => handleViewChange('income')}
                    >
                        Income Report
                    </button>
                    <button
                        className={`px-5 py-2 text-sm rounded-full transition-all duration-300 ${view === 'balance' ? 'bg-blue-600 text-white shadow-md' : 'bg-gray-200 text-gray-700'}`}
                        onClick={() => handleViewChange('balance')}
                    >
                        Balance Sheet
                    </button>
                </div>

                <div className="flex gap-3">
                    <button
                        className="px-4 py-2 bg-green-500 text-white rounded-full hover:bg-green-600 transition-all text-sm"
                        onClick={() => console.log('Data Analysis clicked')}
                    >
                        Data Analysis
                    </button>
                    <button
                        className="px-4 py-2 bg-indigo-500 text-white rounded-full hover:bg-indigo-600 transition-all text-sm"
                        onClick={() => console.log('Report Export clicked')}
                    >
                        Report Export
                    </button>
                </div>
            </div>

            {view === 'income' && (
                <div className="overflow-x-auto">
                    <table className="min-w-full border border-gray-300 rounded-lg">
                        <thead className="bg-blue-100 text-gray-700">
                            <tr>
                                <th className="border px-4 py-2">Item</th>
                                <th className="border px-4 py-2">Current Month</th>
                                <th className="border px-4 py-2">Previous Month</th>
                                <th className="border px-4 py-2">Year To Date</th>
                                <th className="border px-4 py-2">Recovery from JACM</th>
                                <th className="border px-4 py-2">Actual YTD</th>
                                <th className="border px-4 py-2">Budget YTD</th>
                                <th className="border px-4 py-2">Variance</th>
                                <th className="border px-4 py-2">Full Year Budget</th>
                            </tr>
                        </thead>
                        <tbody>
                            {currentData.length === 0 ? (
                                <tr>
                                    <td colSpan={9} className="text-center py-4 text-gray-500">No income data available.</td>
                                </tr>
                            ) : (
                                currentData.map(row => (
                                    <tr key={row.item} className="hover:bg-gray-50 transition">
                                        <td className="border px-4 py-2">{row.item}</td>
                                        <td className="border px-4 py-2">{row.currentMonth.toLocaleString()}</td>
                                        <td className="border px-4 py-2">{row.previousMonth ? row.previousMonth.toLocaleString() : '-'}</td>
                                        <td className="border px-4 py-2">{row.yearToDate.toLocaleString()}</td>
                                        <td className="border px-4 py-2">{row.recoveryFromJACM ? row.recoveryFromJACM.toLocaleString() : '-'}</td>
                                        <td className="border px-4 py-2">{row.actualYTD.toLocaleString()}</td>
                                        <td className="border px-4 py-2">{row.budgetYTD.toLocaleString()}</td>
                                        <td className={`border px-4 py-2 ${row.variance < 0 ? 'text-red-500' : 'text-green-600'}`}>
                                            {row.variance.toLocaleString()}
                                        </td>
                                        <td className="border px-4 py-2">{row.fullYearBudget.toLocaleString()}</td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                    <div className="mt-6 flex justify-center">
                        <ReactPaginate
                            previousLabel="← Prev"
                            nextLabel="Next →"
                            pageCount={pageCount}
                            onPageChange={handlePageClick}
                            forcePage={currentPage}
                            containerClassName="flex gap-2"
                            pageClassName="px-3 py-1 border rounded-md bg-white text-gray-800 hover:bg-blue-100"
                            activeClassName="bg-blue-600 text-white font-bold"
                            previousClassName="px-3 py-1 border rounded-md bg-gray-100"
                            nextClassName="px-3 py-1 border rounded-md bg-gray-100"
                            disabledClassName="opacity-50 cursor-not-allowed"
                        />
                    </div>
                </div>
            )}

            {view === 'balance' && (
                <div className="overflow-x-auto">
                    <table className="min-w-full border border-gray-300 rounded-lg">
                        <thead className="bg-blue-100 text-gray-700">
                            <tr>
                                <th className="border px-4 py-2">Item</th>
                                <th className="border px-4 py-2">Current Month</th>
                                <th className="border px-4 py-2">Previous Month</th>
                                <th className="border px-4 py-2">31-Dec-24</th>
                            </tr>
                        </thead>
                        <tbody>
                            {balanceSheetData.map((row, index) => (
                                <React.Fragment key={index}>
                                    <tr>
                                        <td colSpan={4} className="bg-blue-200 text-lg font-semibold px-4 py-2">{row.item}</td>
                                    </tr>
                                    {row.subItems ? (
                                        row.subItems.map((subRow, subIndex) => (
                                            <tr key={subIndex}>
                                                <td className="border px-4 py-2">{subRow.subItem}</td>
                                                <td className="border px-4 py-2">{subRow.amount.toLocaleString()}</td>
                                                <td className="border px-4 py-2">-</td>
                                                <td className="border px-4 py-2">-</td>
                                            </tr>
                                        ))
                                    ) : (
                                        <tr>
                                            <td className="border px-4 py-2">{row.item}</td>
                                            <td className="border px-4 py-2">{row.total.toLocaleString()}</td>
                                            <td className="border px-4 py-2">-</td>
                                            <td className="border px-4 py-2">-</td>
                                        </tr>
                                    )}
                                </React.Fragment>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
}
