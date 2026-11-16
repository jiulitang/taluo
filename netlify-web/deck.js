/* A shuffled card belongs to its visible position until that card is drawn. */
function shuffleCards(cards) {
  const deck = cards.map(card => ({cardId:card.id,isReversed:Math.random()<.3}));
  for(let i=deck.length-1;i>0;i--){const j=Math.floor(Math.random()*(i+1));[deck[i],deck[j]]=[deck[j],deck[i]];}
  return deck;
}
function moveDeck(direction) {
  const fan=document.querySelector('.fan-container');
  if(fan){fan.stopMotion?.();fan.scrollBy({left:direction*fan.clientWidth*.7,behavior:matchMedia('(prefers-reduced-motion: reduce)').matches?'instant':'smooth'});}
}
function seekDeck(value) {
  const fan=document.querySelector('.fan-container');
  if(fan){fan.stopMotion?.();fan.scrollLeft=Number(value)/100*(fan.scrollWidth-fan.clientWidth);}
}
function bindDeckFan() {
  const fan=document.querySelector('.fan-container');if(!fan)return;
  fan.scrollLeft=state.deckScroll||0;
  let frame=0,dragging=false,moved=false,lastX=0,lastTime=0,velocity=0,downX=0,downScroll=0,blockUntil=0;
  let wheelTarget=fan.scrollLeft;
  fan.stopMotion=()=>{cancelAnimationFrame(frame);frame=0;velocity=0;};
  const reduced=matchMedia('(prefers-reduced-motion: reduce)').matches;
  function update(){
    state.deckScroll=fan.scrollLeft;
    const max=fan.scrollWidth-fan.clientWidth;
    const range=document.getElementById('deck-position');if(range)range.value=max>0?fan.scrollLeft/max*100:0;
    const label=document.getElementById('deck-visible-position');
    if(label)label.textContent=Math.min(state.remainingDeck.length,Math.floor(fan.scrollLeft/30)+1)+' / '+state.remainingDeck.length;
  }
  fan.addEventListener('scroll',update,{passive:true});update();
  fan.addEventListener('dragstart',e=>e.preventDefault());
  fan.addEventListener('pointerdown',e=>{
    if(e.pointerType!=='mouse'||e.button!==0)return;
    fan.stopMotion();wheelTarget=fan.scrollLeft;dragging=true;moved=false;downX=lastX=e.clientX;downScroll=fan.scrollLeft;lastTime=performance.now();velocity=0;
  });
  fan.addEventListener('pointermove',e=>{
    if(!dragging)return;
    if(Math.abs(e.clientX-downX)>6){moved=true;fan.classList.add('is-dragging');if(!fan.hasPointerCapture(e.pointerId))fan.setPointerCapture(e.pointerId);}
    if(!moved)return;
    const now=performance.now(),dt=Math.max(now-lastTime,1);
    velocity=.65*velocity+.35*(lastX-e.clientX)/dt;
    fan.scrollLeft=downScroll+downX-e.clientX;lastX=e.clientX;lastTime=now;e.preventDefault();
  });
  function end(e){
    if(!dragging)return;dragging=false;fan.classList.remove('is-dragging');
    if(fan.hasPointerCapture(e.pointerId))fan.releasePointerCapture(e.pointerId);
    if(!moved)return;
    blockUntil=performance.now()+350;
    if(reduced)return;
    let previous=performance.now();
    function coast(now){const dt=Math.min(now-previous,32);previous=now;velocity*=Math.pow(.94,dt/16);const before=fan.scrollLeft;fan.scrollLeft+=velocity*dt;if(Math.abs(velocity)>.02&&fan.scrollLeft!==before)frame=requestAnimationFrame(coast);else frame=0;}
    frame=requestAnimationFrame(coast);
  }
  fan.addEventListener('pointerup',end);fan.addEventListener('pointercancel',end);
  fan.addEventListener('click',e=>{if(moved||performance.now()<blockUntil){e.preventDefault();e.stopImmediatePropagation();moved=false;}},true);
  fan.addEventListener('wheel',e=>{
    if(Math.abs(e.deltaX)>=Math.abs(e.deltaY))return;
    const delta=e.deltaY*(e.deltaMode===1?16:e.deltaMode===2?fan.clientWidth:1);
    const max=fan.scrollWidth-fan.clientWidth;
    if((delta<0&&fan.scrollLeft<=0)||(delta>0&&fan.scrollLeft>=max-1))return;
    e.preventDefault();
    if(reduced){fan.scrollLeft+=delta;return;}
    if(!frame)wheelTarget=fan.scrollLeft;
    wheelTarget=Math.max(0,Math.min(max,wheelTarget+delta));cancelAnimationFrame(frame);
    function glide(){
      const distance=wheelTarget-fan.scrollLeft;
      if(Math.abs(distance)<.6){fan.scrollLeft=wheelTarget;frame=0;return;}
      fan.scrollLeft+=distance*.24;frame=requestAnimationFrame(glide);
    }
    frame=requestAnimationFrame(glide);
  },{passive:false});
}
function render(){document.querySelector('.fan-container')?.stopMotion?.();renderPage();bindDeckFan();renderWorldbookList();}
