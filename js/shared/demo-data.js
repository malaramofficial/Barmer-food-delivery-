// Development/demo catalog only. Production restaurant/menu data must come from Supabase.
const BFD_DEMO_RESTAURANTS = [
  {id:1,name:'Marwar Rasoi',cuisine:'North Indian • Thali',rating:'4.6',time:'25–35 min',fee:25,emoji:'🍛',items:[['Dal Baati Churma',180,'🥘'],['Special Thali',220,'🍱'],['Paneer Masala',160,'🧀'],['Butter Roti',20,'🫓']]},
  {id:2,name:'Barmer Food Corner',cuisine:'Chinese • Fast Food',rating:'4.4',time:'20–30 min',fee:20,emoji:'🍜',items:[['Veg Noodles',120,'🍜'],['Manchurian',130,'🥢'],['Fried Rice',120,'🍚'],['Spring Roll',90,'🥟']]},
  {id:3,name:'Desert Cafe',cuisine:'Cafe • Snacks',rating:'4.7',time:'15–25 min',fee:15,emoji:'☕',items:[['Masala Chai',30,'☕'],['Cold Coffee',90,'🥤'],['Veg Sandwich',110,'🥪'],['French Fries',100,'🍟']]},
  {id:4,name:'Rajputana Kitchen',cuisine:'Rajasthani • Veg',rating:'4.5',time:'30–40 min',fee:30,emoji:'🥗',items:[['Gatte Ki Sabzi',150,'🥘'],['Ker Sangri',170,'🍲'],['Bajra Roti',35,'🫓'],['Kheer',80,'🍮']]}
];
window.BFD_DEMO_RESTAURANTS = Object.freeze(BFD_DEMO_RESTAURANTS);
