import SwiftUI
import shared

private let fontSize = 17.0

struct WhatsNewFs: View {
    
    @Binding var isPresented: Bool
    
    @EnvironmentObject private var fs: Fs
    
    @State private var vm = WhatsNewVm()
    @State private var scroll = 0
    
    var body: some View {
        
        VMView(vm: vm, stack: .VStack()) { state in
            
            Fs__HeaderTitle(
                title: state.headerTitle,
                scrollToHeader: scroll,
                onClose: {
                    isPresented = false
                }
            )
            
            ScrollViewWithVListener(showsIndicators: false, vScroll: $scroll) {
                
                VStack {
                    
                    ForEachIndexed(state.historyItemsUi) { _, historyItemUi in
                        
                        VStack {
                            
                            HStack {
                                
                                Text(historyItemUi.dateText)
                                    .foregroundColor(c.textSecondary)
                                    .font(.system(size: 15, weight: .light))
                                
                                Spacer()
                                
                                Text(historyItemUi.timeAgoText)
                                    .foregroundColor(c.textSecondary)
                                    .font(.system(size: 15, weight: .light))
                            }
                            
                            HStack {
                                
                                Text(historyItemUi.title)
                                    .foregroundColor(c.text)
                                    .font(.system(size: fontSize, weight: .bold))
                                    .padding(.top, 8)
                                
                                Spacer()
                            }
                            
                            if let text = historyItemUi.text {
                                
                                HStack {
                                    
                                    Text(text)
                                        .foregroundColor(c.textSecondary)
                                        .font(.system(size: 15, weight: .light))
                                        .padding(.top, 8)
                                    
                                    Spacer()
                                }
                            }
                            
                            HStack {
                                
                                if let buttonUi = historyItemUi.buttonUi {
                                    Button(
                                        action: {
                                            if (buttonUi == WhatsNewVm.HistoryItemUiButtonUi.pomodoro) {
                                                fs.ReadmeSheet__open(defaultItem: .pomodoro)
                                            } else {
                                                fatalError()
                                            }
                                        },
                                        label: {
                                            Text(buttonUi.text)
                                                .padding(.top, 8)
                                                .foregroundColor(.blue)
                                        }
                                    )
                                    .offset(x: -halfDpFloor)
                                }
                                
                                Spacer()
                            }
                            
                            if state.historyItemsUi.last != historyItemUi {
                                SheetDividerBg().padding(.top, 16)
                            }
                        }
                        .padding(.top, 16)
                        .padding(.horizontal, H_PADDING)
                    }
                }
                .padding(.bottom, 16)
            }
            
            ZStack {
            }
            .safeAreaPadding(.bottom)
            .padding(.bottom, 16)
        }
    }
}
