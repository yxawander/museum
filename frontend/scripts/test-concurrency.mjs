const base='http://127.0.0.1:8080/api'
async function api(path,{method='GET',body,token}={}){const h={};if(body!==undefined)h['Content-Type']='application/json';if(token)h.Authorization='Bearer '+token;const r=await fetch(base+path,{method,headers:h,body:body===undefined?undefined:JSON.stringify(body)});const t=await r.text();let d;try{d=JSON.parse(t)}catch{d=t};return {status:r.status,body:d}}
async function login(u,p){const r=await api('/auth/login',{method:'POST',body:{username:u,password:p}});return r.body.data.token}
const admin=await login('admin','Admin@123')
const tomorrow=new Date(Date.now()+86400000).toLocaleDateString('sv-SE')
const slot=await api('/admin/slots',{method:'POST',token:admin,body:{exhibitionId:1,visitDate:tomorrow,startTime:'15:00:00',endTime:'16:00:00',capacity:5,status:'OPEN',remark:'并发测试'}})
console.log('slot',slot.body.code,slot.body.data?.id,slot.body.message)
const stamp=Date.now().toString().slice(-6)
const tokens=[]
for(let i=1;i<=10;i++){
  const u=`race${stamp}${i}`
  await api('/auth/register',{method:'POST',body:{username:u,password:'Visitor@123',confirmPassword:'Visitor@123',realName:`并发观众${i}`,phone:`1390000${String(i).padStart(4,'0')}`}})
  tokens.push(await login(u,'Visitor@123'))
}
const payloads=tokens.map((token,i)=>api('/reservations',{method:'POST',token,body:{slotId:slot.body.data.id,visitorName:`并发观众${i+1}`,visitorIdCard:`42010619980808${String(1000+i)}`,visitorPhone:`1390000${String(i+1).padStart(4,'0')}`,ticketCount:1}}))
const results=await Promise.all(payloads)
const success=results.filter(r=>r.body.code===200).length
const conflict=results.filter(r=>r.body.code===409).length
const slots=await api(`/admin/slots?page=1&size=100&exhibitionId=1&date=${tomorrow}`,{token:admin})
const row=slots.body.data.records.find(s=>s.id===slot.body.data.id)
console.log('success',success,'conflict',conflict,'booked',row?.bookedCount,'remaining',row?.remaining)
if(success!==5||conflict!==5||row?.bookedCount!==5) { console.error('CONCURRENCY TEST FAILED'); process.exitCode=1 } else console.log('CONCURRENCY TEST PASSED: 5/10 success, no oversell')
